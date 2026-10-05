package com.fitelyback.backend.modules.tarjetas;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.tarjetas.dto.NivelDto;
import com.fitelyback.backend.modules.tarjetas.dto.PlantillaRequest;
import com.fitelyback.backend.modules.tarjetas.dto.PlantillaResponse;
import com.fitelyback.backend.modules.tenant.NegocioRepository;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import com.fitelyback.backend.modules.ubicaciones.UbicacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TarjetaPlantillaService {

    private static final String COLOR_FONDO_POR_DEFECTO = "#1E40AF";
    private static final String COLOR_TEXTO_POR_DEFECTO = "#FFFFFF";

    private final TarjetaPlantillaRepository plantillaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final NegocioRepository negocioRepository;

    @Transactional(readOnly = true)
    public List<PlantillaResponse> listar(Long negocioId, Long ubicacionId, TipoTarjeta tipo) {
        List<TarjetaPlantilla> plantillas = ubicacionId != null
                ? plantillaRepository.findByNegocioIdAndUbicacionIdOrderByNombreAsc(negocioId, ubicacionId)
                : plantillaRepository.findByNegocioIdOrderByNombreAsc(negocioId);

        return plantillas.stream()
                .filter(p -> tipo == null || p.getTipo() == tipo)
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PlantillaResponse obtener(Long id, Long negocioId) {
        return aResponse(buscar(id, negocioId));
    }

    @Transactional
    public PlantillaResponse crear(PlantillaRequest request, Long negocioId) {
        Ubicacion ubicacion = buscarUbicacion(request.ubicacionId(), negocioId);

        if (plantillaRepository.existsByUbicacionIdAndNombreIgnoreCase(ubicacion.getId(), request.nombre())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una tarjeta con ese nombre en la sede");
        }

        TarjetaPlantilla plantilla = TarjetaPlantilla.builder()
                .tipo(request.tipo())
                .negocio(negocioRepository.getReferenceById(negocioId))
                .build();

        aplicarDatos(plantilla, request, ubicacion, negocioId);
        return aResponse(plantillaRepository.save(plantilla));
    }

    @Transactional
    public PlantillaResponse actualizar(Long id, PlantillaRequest request, Long negocioId) {
        TarjetaPlantilla plantilla = buscar(id, negocioId);

        // Cambiar el tipo dejaría sin sentido las tarjetas ya emitidas
        if (plantilla.getTipo() != request.tipo()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El tipo de una tarjeta no se puede cambiar");
        }

        Ubicacion ubicacion = buscarUbicacion(request.ubicacionId(), negocioId);

        if (plantillaRepository.existsByUbicacionIdAndNombreIgnoreCaseAndIdNot(
                ubicacion.getId(), request.nombre(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una tarjeta con ese nombre en la sede");
        }

        aplicarDatos(plantilla, request, ubicacion, negocioId);
        return aResponse(plantilla);
    }

    @Transactional
    public void eliminar(Long id, Long negocioId) {
        plantillaRepository.delete(buscar(id, negocioId));
    }

    private TarjetaPlantilla buscar(Long id, Long negocioId) {
        return plantillaRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta no encontrada"));
    }

    private Ubicacion buscarUbicacion(Long ubicacionId, Long negocioId) {
        return ubicacionRepository.findByIdAndNegocioId(ubicacionId, negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ubicación no encontrada"));
    }

    // Copia los datos comunes y aplica las reglas propias de cada tipo de tarjeta
    private void aplicarDatos(TarjetaPlantilla plantilla, PlantillaRequest request,
                              Ubicacion ubicacion, Long negocioId) {
        plantilla.setNombre(request.nombre());
        plantilla.setUbicacion(ubicacion);
        plantilla.setPrecioProducto(request.precioProducto());
        if (request.activa() != null) {
            plantilla.setActiva(request.activa());
        }

        // Diseño visual
        plantilla.setColorFondo(request.colorFondo() != null ? request.colorFondo() : COLOR_FONDO_POR_DEFECTO);
        plantilla.setColorTexto(request.colorTexto() != null ? request.colorTexto() : COLOR_TEXTO_POR_DEFECTO);
        plantilla.setLogoUrl(request.logoUrl());
        plantilla.setImagenFondoUrl(request.imagenFondoUrl());
        plantilla.setIconoSelloActivo(request.iconoSelloActivo());
        plantilla.setIconoSelloInactivo(request.iconoSelloInactivo());

        // Reverso
        plantilla.setDescripcion(request.descripcion());
        plantilla.setTelefono(request.telefono());
        plantilla.setSitioWeb(request.sitioWeb());

        // Se limpian los campos de los otros tipos para no guardar datos sin uso
        plantilla.setMetaSellos(null);
        plantilla.setRecompensa(null);
        plantilla.setSiguientePlantilla(null);
        plantilla.setSaldoInicial(null);
        plantilla.getNiveles().clear();

        switch (plantilla.getTipo()) {
            case ESTAMPILLAS -> {
                if (request.metaSellos() == null) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "Una tarjeta de estampillas necesita la meta de sellos");
                }
                if (request.recompensa() == null || request.recompensa().isBlank()) {
                    throw new ApiException(HttpStatus.BAD_REQUEST,
                            "Una tarjeta de estampillas necesita una recompensa");
                }
                plantilla.setMetaSellos(request.metaSellos());
                plantilla.setRecompensa(request.recompensa());
                plantilla.setSiguientePlantilla(
                        resolverSiguiente(request.siguientePlantillaId(), plantilla, ubicacion, negocioId));
            }
            case NIVELES -> plantilla.getNiveles().addAll(validarNiveles(request.niveles()));
            case GIFTCARD -> plantilla.setSaldoInicial(
                    request.saldoInicial() != null ? request.saldoInicial() : BigDecimal.ZERO);
        }
    }

    // La tarjeta siguiente debe ser otra tarjeta de estampillas de la misma sede
    private TarjetaPlantilla resolverSiguiente(Long siguienteId, TarjetaPlantilla plantilla,
                                               Ubicacion ubicacion, Long negocioId) {
        if (siguienteId == null) {
            return null;
        }
        if (siguienteId.equals(plantilla.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Para repetir la misma tarjeta deja vacía la tarjeta siguiente");
        }

        TarjetaPlantilla siguiente = plantillaRepository.findByIdAndNegocioId(siguienteId, negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tarjeta siguiente no encontrada"));

        if (siguiente.getTipo() != TipoTarjeta.ESTAMPILLAS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La tarjeta siguiente debe ser de estampillas");
        }
        if (!siguiente.getUbicacion().getId().equals(ubicacion.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La tarjeta siguiente debe ser de la misma sede");
        }
        return siguiente;
    }

    private List<NivelConfig> validarNiveles(List<NivelDto> niveles) {
        if (niveles == null || niveles.size() < 2) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Una tarjeta de niveles necesita al menos dos niveles");
        }

        Set<String> nombres = new HashSet<>();
        Set<Integer> minimos = new HashSet<>();
        for (NivelDto nivel : niveles) {
            if (!nombres.add(nivel.nombre().trim().toLowerCase())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Los nombres de los niveles no pueden repetirse");
            }
            if (!minimos.add(nivel.visitasMinimas())) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Dos niveles no pueden tener el mismo mínimo de visitas");
            }
        }
        if (!minimos.contains(0)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El primer nivel debe empezar en 0 visitas");
        }

        return niveles.stream()
                .sorted(Comparator.comparing(NivelDto::visitasMinimas))
                .map(n -> new NivelConfig(n.nombre().trim(), n.visitasMinimas(), n.beneficio()))
                .toList();
    }

    private PlantillaResponse aResponse(TarjetaPlantilla p) {
        List<NivelDto> niveles = p.getNiveles().stream()
                .map(n -> new NivelDto(n.getNombre(), n.getVisitasMinimas(), n.getBeneficio()))
                .toList();

        TarjetaPlantilla siguiente = p.getSiguientePlantilla();

        return new PlantillaResponse(
                p.getId(),
                p.getTipo(),
                p.getNombre(),
                p.getUbicacion().getId(),
                p.getUbicacion().getNombre(),
                p.isActiva(),
                p.getPrecioProducto(),
                p.getColorFondo(),
                p.getColorTexto(),
                p.getLogoUrl(),
                p.getImagenFondoUrl(),
                p.getIconoSelloActivo(),
                p.getIconoSelloInactivo(),
                p.getDescripcion(),
                p.getTelefono(),
                p.getSitioWeb(),
                p.getMetaSellos(),
                p.getRecompensa(),
                siguiente != null ? siguiente.getId() : null,
                siguiente != null ? siguiente.getNombre() : null,
                niveles,
                p.getSaldoInicial(),
                p.getFechaCreacion()
        );
    }
}