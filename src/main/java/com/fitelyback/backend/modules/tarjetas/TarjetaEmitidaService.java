package com.fitelyback.backend.modules.tarjetas;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.tarjetas.dto.EmitirTarjetaRequest;
import com.fitelyback.backend.modules.tarjetas.dto.PlantillaPublicaResponse;
import com.fitelyback.backend.modules.tarjetas.dto.RegistroClienteRequest;
import com.fitelyback.backend.modules.tarjetas.dto.TarjetaEmitidaResponse;
import com.fitelyback.backend.modules.tarjetas.dto.TarjetaPublicaResponse;
import com.fitelyback.backend.modules.tenant.NegocioRepository;
import com.fitelyback.backend.modules.tenant.clientes.Cliente;
import com.fitelyback.backend.modules.tenant.clientes.ClienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TarjetaEmitidaService {

    private static final int MESES_DE_VIGENCIA = 12;
    private static final String PREFIJO_CODIGO = "T-";

    private final TarjetaEmitidaRepository tarjetaRepository;
    private final TarjetaPlantillaRepository plantillaRepository;
    private final ClienteRepository clienteRepository;
    private final NegocioRepository negocioRepository;

    // ---------- Panel (con token) ----------

    // Filtros opcionales: sede, plantilla y cliente
    @Transactional(readOnly = true)
    public List<TarjetaEmitidaResponse> listar(Long negocioId, Long ubicacionId, Long plantillaId, Long clienteId) {
        return tarjetaRepository.findByNegocioIdOrderByFechaEmisionDesc(negocioId)
                .stream()
                .filter(t -> ubicacionId == null || t.getUbicacion().getId().equals(ubicacionId))
                .filter(t -> plantillaId == null || t.getPlantilla().getId().equals(plantillaId))
                .filter(t -> clienteId == null || t.getCliente().getId().equals(clienteId))
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TarjetaEmitidaResponse obtener(Long id, Long negocioId) {
        return aResponse(buscar(id, negocioId));
    }

    @Transactional
    public TarjetaEmitidaResponse emitir(EmitirTarjetaRequest request, Long negocioId) {
        Cliente cliente = clienteRepository.findByIdAndNegocioId(request.clienteId(), negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));

        TarjetaPlantilla plantilla = plantillaRepository.findByIdAndNegocioId(request.plantillaId(), negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada"));

        return aResponse(crearTarjeta(cliente, plantilla));
    }

    @Transactional
    public void anular(Long id, Long negocioId) {
        buscar(id, negocioId).setEstado(EstadoTarjeta.ANULADA);
    }

    // ---------- Público (sin token) ----------

    // Tarjetas que el cliente puede elegir al registrarse
    @Transactional(readOnly = true)
    public List<PlantillaPublicaResponse> plantillasPublicas(Long negocioId) {
        verificarNegocio(negocioId);
        return plantillaRepository.findByNegocioIdAndActivaTrueOrderByNombreAsc(negocioId)
                .stream()
                .map(p -> new PlantillaPublicaResponse(
                        p.getId(),
                        p.getTipo(),
                        p.getNombre(),
                        p.getMetaSellos(),
                        p.getRecompensa(),
                        p.getUbicacion().getId(),
                        p.getUbicacion().getNombre()))
                .toList();
    }

    // "Crear mi tarjeta": registra al cliente (o lo reutiliza si su teléfono ya existe) y le emite la tarjeta
    @Transactional
    public TarjetaPublicaResponse registrar(Long negocioId, RegistroClienteRequest request) {
        verificarNegocio(negocioId);

        TarjetaPlantilla plantilla = plantillaRepository.findByIdAndNegocioId(request.plantillaId(), negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada"));

        String telefono = request.telefono().trim();

        Cliente cliente = clienteRepository.findByNegocioIdAndTelefono(negocioId, telefono)
                .orElseGet(() -> clienteRepository.save(Cliente.builder()
                        .nombre(request.nombre().trim())
                        .apellido(request.apellido().trim())
                        .telefono(telefono)
                        .fechaNacimiento(request.fechaNacimiento())
                        .negocio(negocioRepository.getReferenceById(negocioId))
                        .build()));

        return aPublica(crearTarjeta(cliente, plantilla));
    }

    // Página de la tarjeta del cliente
    @Transactional(readOnly = true)
    public TarjetaPublicaResponse verPorCodigo(String codigo) {
        return aPublica(tarjetaRepository.findByCodigo(codigo)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada")));
    }

    // ---------- Compartido ----------

    // Crea la tarjeta de un cliente. Lo usan la emisión, el registro público y el canje de recompensas.
    @Transactional
    public TarjetaEmitida crearTarjeta(Cliente cliente, TarjetaPlantilla plantilla) {
        if (!plantilla.isActiva()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Esta tarjeta no está disponible");
        }
        if (tarjetaRepository.existsByClienteIdAndPlantillaIdAndEstado(
                cliente.getId(), plantilla.getId(), EstadoTarjeta.ACTIVA)) {
            throw new ApiException(HttpStatus.CONFLICT, "El cliente ya tiene una tarjeta activa de este programa");
        }

        LocalDateTime ahora = LocalDateTime.now();

        TarjetaEmitida tarjeta = TarjetaEmitida.builder()
                .codigo(PREFIJO_CODIGO + UUID.randomUUID())
                .estado(EstadoTarjeta.ACTIVA)
                .sellos(0)
                .visitas(0)
                .saldo(plantilla.getTipo() == TipoTarjeta.GIFTCARD ? plantilla.getSaldoInicial() : null)
                .fechaEmision(ahora)
                .fechaVencimiento(ahora.toLocalDate().plusMonths(MESES_DE_VIGENCIA))
                .plantilla(plantilla)
                .cliente(cliente)
                .ubicacion(plantilla.getUbicacion())
                .negocio(plantilla.getNegocio())
                .build();

        return tarjetaRepository.save(tarjeta);
    }

    private void verificarNegocio(Long negocioId) {
        if (!negocioRepository.existsById(negocioId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Negocio no encontrado");
        }
    }

    private TarjetaEmitida buscar(Long id, Long negocioId) {
        return tarjetaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta emitida no encontrada"));
    }

    private boolean estaVencida(TarjetaEmitida tarjeta) {
        return tarjeta.getFechaVencimiento().isBefore(LocalDate.now());
    }

    private boolean estaCompleta(TarjetaEmitida tarjeta) {
        Integer meta = tarjeta.getPlantilla().getMetaSellos();
        return meta != null && tarjeta.getSellos() >= meta;
    }

    private long canjesDe(Cliente cliente) {
        return tarjetaRepository.countByClienteIdAndEstado(cliente.getId(), EstadoTarjeta.CANJEADA);
    }

    // Nivel alcanzado según las visitas acumuladas (solo tarjetas de NIVELES)
    private String nivelActual(TarjetaEmitida tarjeta) {
        if (tarjeta.getPlantilla().getTipo() != TipoTarjeta.NIVELES) {
            return null;
        }
        return tarjeta.getPlantilla().getNiveles().stream()
                .filter(nivel -> nivel.getVisitasMinimas() <= tarjeta.getVisitas())
                .reduce((anterior, actual) -> actual)
                .map(NivelConfig::getNombre)
                .orElse(null);
    }

    private TarjetaEmitidaResponse aResponse(TarjetaEmitida t) {
        TarjetaPlantilla plantilla = t.getPlantilla();
        Cliente cliente = t.getCliente();

        return new TarjetaEmitidaResponse(
                t.getId(),
                t.getCodigo(),
                t.getEstado(),
                estaVencida(t),
                plantilla.getTipo(),
                plantilla.getId(),
                plantilla.getNombre(),
                t.getUbicacion().getId(),
                t.getUbicacion().getNombre(),
                cliente.getId(),
                cliente.getNombre() + " " + cliente.getApellido(),
                cliente.getTelefono(),
                t.getSellos(),
                plantilla.getMetaSellos(),
                estaCompleta(t),
                plantilla.getRecompensa(),
                t.getVisitas(),
                nivelActual(t),
                t.getSaldo(),
                canjesDe(cliente),
                t.getFechaEmision(),
                t.getFechaVencimiento()
        );
    }

    private TarjetaPublicaResponse aPublica(TarjetaEmitida t) {
        TarjetaPlantilla plantilla = t.getPlantilla();

        return new TarjetaPublicaResponse(
                t.getCodigo(),
                t.getEstado(),
                estaVencida(t),
                plantilla.getTipo(),
                plantilla.getNombre(),
                t.getUbicacion().getNombre(),
                t.getCliente().getNombre(),
                t.getSellos(),
                plantilla.getMetaSellos(),
                estaCompleta(t),
                plantilla.getRecompensa(),
                t.getVisitas(),
                nivelActual(t),
                t.getSaldo(),
                canjesDe(t.getCliente()),
                t.getFechaVencimiento(),
                plantilla.getColorFondo(),
                plantilla.getColorTexto(),
                plantilla.getLogoUrl(),
                plantilla.getImagenFondoUrl(),
                plantilla.getIconoSelloActivo(),
                plantilla.getIconoSelloInactivo(),
                plantilla.getDescripcion(),
                plantilla.getTelefono(),
                plantilla.getSitioWeb()
        );
    }
}