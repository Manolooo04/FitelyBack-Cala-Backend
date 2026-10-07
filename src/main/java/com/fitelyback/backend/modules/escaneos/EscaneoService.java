package com.fitelyback.backend.modules.escaneos;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.escaneos.dto.AbrirEscanerRequest;
import com.fitelyback.backend.modules.escaneos.dto.CanjeResponse;
import com.fitelyback.backend.modules.escaneos.dto.EscaneoResponse;
import com.fitelyback.backend.modules.escaneos.dto.EscanerAbiertoResponse;
import com.fitelyback.backend.modules.escaneos.dto.EstampillasRequest;
import com.fitelyback.backend.modules.escaneos.dto.RecompensaEscaneadaResponse;
import com.fitelyback.backend.modules.escaneos.dto.SaldoRequest;
import com.fitelyback.backend.modules.escaneos.dto.TarjetaEscaneadaResponse;
import com.fitelyback.backend.modules.recompensas.EstadoRecompensa;
import com.fitelyback.backend.modules.recompensas.Recompensa;
import com.fitelyback.backend.modules.recompensas.RecompensaRepository;
import com.fitelyback.backend.modules.recompensas.RecompensaService;
import com.fitelyback.backend.modules.tarjetas.EstadoTarjeta;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitida;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitidaRepository;
import com.fitelyback.backend.modules.tarjetas.TarjetaEmitidaService;
import com.fitelyback.backend.modules.tarjetas.TarjetaPlantilla;
import com.fitelyback.backend.modules.tarjetas.TipoTarjeta;
import com.fitelyback.backend.modules.tenant.auth.Rol;
import com.fitelyback.backend.modules.tenant.auth.Usuario;
import com.fitelyback.backend.modules.tenant.auth.UsuarioRepository;
import com.fitelyback.backend.modules.tenant.clientes.Cliente;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EscaneoService {

    private static final String PREFIJO_TARJETA = "T-";
    private static final String PREFIJO_RECOMPENSA = "R-";
    private static final int HORAS_DE_TURNO = 12;
    private static final int HORAS_ENTRE_VISITAS = 12;
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm");

    private final UsuarioRepository usuarioRepository;
    private final UbicacionRepository ubicacionRepository;
    private final TarjetaEmitidaRepository tarjetaRepository;
    private final EscaneoTarjetaRepository escaneoRepository;
    private final RecompensaRepository recompensaRepository;
    private final TarjetaEmitidaService tarjetaEmitidaService;
    private final RecompensaService recompensaService;
    private final PasswordEncoder passwordEncoder;

    // ---------- Ingreso con el PIN de la tienda ----------

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

    // ---------- Escanear QR tarjeta ----------

    @Transactional(readOnly = true)
    public TarjetaEscaneadaResponse verTarjeta(String codigo, UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);
        return aRespuesta(buscarTarjeta(codigo, sedeDelEscaner(usuario), actual));
    }

    // Estampillas: suma (cantidad positiva) o resta (cantidad negativa)
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
        validarUtilizable(tarjeta);

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

        // Genera la recompensa si la tarjeta se completó, o la anula si dejó de estarlo
        recompensaService.sincronizar(tarjeta);

        return aRespuesta(tarjeta);
    }

    // Niveles: cada escaneo registra una visita; el nivel sube según las visitas acumuladas
    @Transactional
    public TarjetaEscaneadaResponse registrarVisita(String codigo, UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);
        Ubicacion sede = sedeDelEscaner(usuario);
        TarjetaEmitida tarjeta = buscarTarjeta(codigo, sede, actual);

        if (tarjeta.getPlantilla().getTipo() != TipoTarjeta.NIVELES) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta no acumula visitas");
        }
        validarUtilizable(tarjeta);

        // Evita sumar dos visitas seguidas por un doble escaneo
        boolean visitaReciente = escaneoRepository.existsByTarjetaIdAndTipoAndFechaAfter(
                tarjeta.getId(), TipoEscaneo.VISITA, LocalDateTime.now().minusHours(HORAS_ENTRE_VISITAS));
        if (visitaReciente) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Esta tarjeta ya registró una visita en las últimas " + HORAS_ENTRE_VISITAS + " horas");
        }

        int antes = tarjeta.getVisitas();
        int despues = antes + 1;
        tarjeta.setVisitas(despues);

        escaneoRepository.save(EscaneoTarjeta.builder()
                .tipo(TipoEscaneo.VISITA)
                .cantidad(1)
                .valorAntes(antes)
                .valorDespues(despues)
                .tarjeta(tarjeta)
                .usuario(usuario)
                .ubicacion(sede)
                .negocio(tarjeta.getNegocio())
                .build());

        return aRespuesta(tarjeta);
    }

    // Giftcard: recarga (monto positivo) o consumo (monto negativo)
    @Transactional
    public TarjetaEscaneadaResponse ajustarSaldo(String codigo, SaldoRequest request,
                                                 UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);
        Ubicacion sede = sedeDelEscaner(usuario);
        TarjetaEmitida tarjeta = buscarTarjeta(codigo, sede, actual);
        BigDecimal monto = request.monto();

        if (monto.signum() == 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El monto no puede ser cero");
        }
        if (tarjeta.getPlantilla().getTipo() != TipoTarjeta.GIFTCARD) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta no maneja saldo");
        }
        validarUtilizable(tarjeta);

        BigDecimal antes = tarjeta.getSaldo() != null ? tarjeta.getSaldo() : BigDecimal.ZERO;
        BigDecimal despues = antes.add(monto);

        if (despues.signum() < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Saldo insuficiente. Disponible: S/ " + antes);
        }

        tarjeta.setSaldo(despues);

        escaneoRepository.save(EscaneoTarjeta.builder()
                .tipo(monto.signum() > 0 ? TipoEscaneo.RECARGA : TipoEscaneo.CONSUMO)
                .monto(monto.abs())
                .tarjeta(tarjeta)
                .usuario(usuario)
                .ubicacion(sede)
                .negocio(tarjeta.getNegocio())
                .build());

        return aRespuesta(tarjeta);
    }

    // ---------- Escanear QR recompensa ----------

    // Datos para la pantalla "Confirmar beneficio"
    @Transactional(readOnly = true)
    public RecompensaEscaneadaResponse verRecompensa(String codigo, UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);
        Recompensa recompensa = buscarRecompensa(codigo, sedeDelEscaner(usuario), actual);
        TarjetaEmitida tarjeta = recompensa.getTarjeta();

        return new RecompensaEscaneadaResponse(
                recompensa.getCodigo(),
                recompensa.getDescripcion(),
                nombreCompleto(tarjeta.getCliente()),
                tarjeta.getPlantilla().getNombre());
    }

    // Entrega el premio, cierra la tarjeta y le emite al cliente la tarjeta siguiente
    @Transactional
    public CanjeResponse canjearRecompensa(String codigo, UsuarioAutenticado actual) {
        Usuario usuario = usuarioActual(actual);
        Ubicacion sede = sedeDelEscaner(usuario);
        Recompensa recompensa = buscarRecompensa(codigo, sede, actual);
        TarjetaEmitida tarjeta = recompensa.getTarjeta();
        TarjetaPlantilla plantilla = tarjeta.getPlantilla();
        Cliente cliente = tarjeta.getCliente();

        tarjeta.setEstado(EstadoTarjeta.CANJEADA);
        recompensa.setEstado(EstadoRecompensa.CANJEADA);
        recompensa.setFechaCanje(LocalDateTime.now());
        recompensa.setCanjeadoPor(usuario);

        // La tarjeta siguiente es la que indica la plantilla; si no indica ninguna, se repite la misma
        TarjetaPlantilla siguiente = plantilla.getSiguientePlantilla() != null
                ? plantilla.getSiguientePlantilla()
                : plantilla;

        TarjetaEmitida nueva = null;
        boolean puedeEmitirse = siguiente.isActiva()
                && !tarjetaRepository.existsByClienteIdAndPlantillaIdAndEstado(
                cliente.getId(), siguiente.getId(), EstadoTarjeta.ACTIVA);
        if (puedeEmitirse) {
            nueva = tarjetaEmitidaService.crearTarjeta(cliente, siguiente);
        }
        recompensa.setTarjetaSiguiente(nueva);

        escaneoRepository.save(EscaneoTarjeta.builder()
                .tipo(TipoEscaneo.CANJE)
                .valorAntes(tarjeta.getSellos())
                .valorDespues(tarjeta.getSellos())
                .tarjeta(tarjeta)
                .usuario(usuario)
                .ubicacion(sede)
                .negocio(tarjeta.getNegocio())
                .build());

        return new CanjeResponse(
                recompensa.getDescripcion(),
                nombreCompleto(cliente),
                nueva != null ? nueva.getCodigo() : null,
                nueva != null ? siguiente.getNombre() : null);
    }

    // ---------- Apoyo ----------

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

    // Una tarjeta solo admite movimientos si está activa y vigente
    private void validarUtilizable(TarjetaEmitida tarjeta) {
        if (tarjeta.getEstado() != EstadoTarjeta.ACTIVA) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta ya no está activa");
        }
        if (tarjeta.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta está vencida");
        }
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

    private Recompensa buscarRecompensa(String codigo, Ubicacion sede, UsuarioAutenticado actual) {
        if (codigo == null || !codigo.startsWith(PREFIJO_RECOMPENSA)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Formato de QR de beneficio inválido");
        }

        Recompensa recompensa = recompensaRepository.findByCodigo(codigo)
                .filter(r -> actual.negocioId().equals(r.getNegocio().getId()))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Beneficio no encontrado"));

        TarjetaEmitida tarjeta = recompensa.getTarjeta();

        if (recompensa.getEstado() == EstadoRecompensa.CANJEADA) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Este beneficio \"" + recompensa.getDescripcion() + "\" ya fue canjeado por "
                            + nombreCompleto(tarjeta.getCliente()) + " el "
                            + recompensa.getFechaCanje().format(FORMATO_FECHA)
                            + ". No se puede canjear nuevamente.");
        }
        if (!tarjeta.getUbicacion().getId().equals(sede.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Este beneficio pertenece a la sede " + tarjeta.getUbicacion().getNombre());
        }
        if (tarjeta.getFechaVencimiento().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La tarjeta de este beneficio está vencida");
        }
        return recompensa;
    }

    private String nombreCompleto(Cliente cliente) {
        return cliente.getNombre() + " " + cliente.getApellido();
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