package com.fitelyback.backend.modules.escaneos;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.escaneos.dto.AbrirEscanerRequest;
import com.fitelyback.backend.modules.escaneos.dto.EscaneoResponse;
import com.fitelyback.backend.modules.escaneos.dto.EscanerAbiertoResponse;
import com.fitelyback.backend.modules.escaneos.dto.EstampillasRequest;
import com.fitelyback.backend.modules.escaneos.dto.TarjetaEscaneadaResponse;
import com.fitelyback.backend.modules.tarjetas.EstadoTarjeta;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitida;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitidaRepository;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitidaService;
import com.fitelyback.backend.modules.tarjetas.TarjetaPlantilla;
import com.fitelyback.backend.modules.tarjetas.TipoTarjeta;
import com.fitelyback.backend.modules.tenant.auth.Rol;
import com.fitelyback.backend.modules.tenant.auth.Usuario;
import com.fitelyback.backend.modules.tenant.auth.UsuarioRepository;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import com.fitelyback.backend.modules.ubicaciones.UbicacionRepository;
import com.fitelyback.backend.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EscaneoService {

    private static final String PREFIJO_TARJETA = "T-";
    private static final int HORAS_DE_TURNO = 12;

    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final TarjetaEmitidaRepository tarjetaRepository;
    private final EscaneoTarjetaRepository escaneoRepository;
    private final TarjetaEmitidaService tarjetaEmitidaService;
    private final PasswordEncoder passwordEncoder;

    // El trabajador elige una sede e ingresa el PIN de la tienda para empezar a escanear
    @Transactional
    public EscanerAbiertoResponse abrir(AbrirEscanerRequest request, UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);

        Ubicacion sede = ubicacionRepository.findByIdAndNegocioId(request.ubicacionId(), actual.negocioId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ubicación no encontrada"));

        boolean tieneAcceso = usuario.getRol() == Rol.ADMIN
                || usuario.getUbicaciones().stream().anyMatch(u -> u.getId().equals(sede.getId()));
        if (!tieneAcceso) {
            throw new ApiException(HttpStatus.FORBIDDEN, "No tienes acceso a esta sede");
        }
        if (sede.getPin() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta sede aún no tiene un PIN configurado");
        }
        if (!passwordEncoder.matches(request.pin(), sede.getPin())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PIN incorrecto");
        }

        usuario.setUbicacion(sede);
        usuario.setEscanerAbiertoHasta(LocalDateTime.now().plusHours(HORAS_DE_TURNO));

        return new EscanerAbiertoResponse(sede.getId(), sede.getNombre(), usuario.getEscanerAbiertoHasta());
    }

    @Transactional
    public void cerrar(UsuarioAutenticado actual) {
        usuarioActual(actual).setEscanerAbiertoHasta(null);
    }

    // "Escanear QR tarjeta": muestra la tarjeta con su progreso e historial
    @Transactional(readOnly = true)
    public TarjetaEscaneadaResponse verTarjeta(String codigo, UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);
        return aRespuesta(buscarTarjeta(codigo, sedeDelEscaner(usuario), actual));
    }

    // Suma (cantidad positiva) o resta (cantidad negativa) estampillas
    @Transactional
    public TarjetaEscaneadaResponse ajustarEstampillas(String codigo, EstampillasRequest request,
                                                       UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);
        Ubicacion sede = sedeDelEscaner(usuario);
        TarjetaEmitida tarjeta = buscarTarjeta(codigo, sede, actual);
        TarjetaPlantilla plantilla = tarjeta.getPlantilla();
        int cantidad = request.cantidad();

        if (cantidad == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La cantidad no puede ser cero");
        }
        if (plantilla.getTipo() != TipoTarjeta.ESTAMPILLAS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta no acumula estampillas");
        }
        if (tarjeta.getEstado() != EstadoTarjeta.ACTIVA) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta ya no está activa");
        }
        if (tarjeta.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta está vencida");
        }

        int meta = plantilla.getMetaSellos();
        int antes = tarjeta.getSellos();
        int despues = antes + cantidad;

        if (cantidad > 0 && antes >= meta) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "La tarjeta ya está completa. El cliente debe canjear su recompensa");
        }
        if (despues > meta) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Solo faltan " + (meta - antes) + " estampillas para completar la tarjeta");
        }
        if (despues < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La tarjeta solo tiene " + antes + " estampillas");
        }

        tarjeta.setSellos(despues);

        BigDecimal precio = plantilla.getPrecioProducto();
        escaneoRepository.save(EscaneoTarjeta.builder()
                .tipo(TipoEscaneo.SELLO)
                .cantidad(cantidad)
                .valorAntes(antes)
                .valorDespues(despues)
                .monto(precio != null ? precio.multiply(BigDecimal.valueOf(cantidad)) : null)
                .tarjeta(tarjeta)
                .usuario(usuario)
                .ubicacion(sede)
                .negocio(tarjeta.getNegocio())
                .build());

        return aRespuesta(tarjeta);
    }

    private Usuario usuarioActual(UsuarioAutenticado actual) {
        return usuarioRepository.findByEmail(actual.email())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Usuario no encontrado"));
    }

    // Sede donde el trabajador tiene el escáner abierto; falla si no lo abrió o si el turno expiró
    private Ubicacion sedeDelEscaner(Usuario usuario) {
        boolean abierto = usuario.getUbicacion() != null
                && usuario.getEscanerAbiertoHasta() != null
                && usuario.getEscanerAbiertoHasta().isAfter(LocalDateTime.now());
        if (!abierto) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Primero debes ingresar el PIN de la tienda para empezar a escanear");
        }
        return usuario.getUbicacion();
    }

    private TarjetaEmitida buscarTarjeta(String codigo, Ubicacion sede, UsuarioAutenticado actual) {
        if (codigo == null || !codigo.startsWith(PREFIJO_TARJETA)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Formato de QR inválido");
        }

        TarjetaEmitida tarjeta = tarjetaRepository.findByCodigo(codigo)
                .filter(t -> actual.negocioId().equals(t.getNegocio().getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada"));

        if (!tarjeta.getUbicacion().getId().equals(sede.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Esta tarjeta pertenece a la sede " + tarjeta.getUbicacion().getNombre());
        }
        return tarjeta;
    }

    private TarjetaEscaneadaResponse aRespuesta(TarjetaEmitida tarjeta) {
        List<EscaneoResponse> historial = escaneoRepository.findTop20ByTarjetaIdOrderByFechaDesc(tarjeta.getId())
                .stream()
                .map(e -> new EscaneoResponse(
                        e.getId(),
                        e.getTipo(),
                        e.getCantidad(),
                        e.getValorAntes(),
                        e.getValorDespues(),
                        e.getMonto(),
                        e.getUsuario().getNombre() + " " + e.getUsuario().getApellido(),
                        e.getUbicacion().getNombre(),
                        e.getFecha()))
                .toList();

        return new TarjetaEscaneadaResponse(tarjetaEmitidaService.resumen(tarjeta), historial);
    }
}