package com.fitelyback.backend.modules.ubicaciones;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.tenant.NegocioRepository;
import com.fitelyback.backend.modules.ubicaciones.dto.UbicacionRequest;
import com.fitelyback.backend.modules.ubicaciones.dto.UbicacionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UbicacionService {

    private static final int RADIO_POR_DEFECTO = 300;

    private final UbicacionRepository ubicacionRepository;
    private final NegocioRepository negocioRepository;

    @Transactional(readOnly = true)
    public List<UbicacionResponse> listar(Long negocioId) {
        return ubicacionRepository.findByNegocioIdOrderByNombreAsc(negocioId)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public UbicacionResponse obtener(Long id, Long negocioId) {
        return aResponse(buscar(id, negocioId));
    }

    @Transactional
    public UbicacionResponse crear(UbicacionRequest request, Long negocioId) {
        if (ubicacionRepository.existsByNegocioIdAndNombreIgnoreCase(negocioId, request.nombre())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una ubicación con ese nombre");
        }

        Ubicacion ubicacion = Ubicacion.builder()
                .nombre(request.nombre())
                .direccion(request.direccion())
                .telefono(request.telefono())
                .horario(request.horario())
                .latitud(request.latitud())
                .longitud(request.longitud())
                .radioMetros(radioO(request.radioMetros()))
                .negocio(negocioRepository.getReferenceById(negocioId))
                .build();

        return aResponse(ubicacionRepository.save(ubicacion));
    }

    @Transactional
    public UbicacionResponse actualizar(Long id, UbicacionRequest request, Long negocioId) {
        Ubicacion ubicacion = buscar(id, negocioId);

        if (ubicacionRepository.existsByNegocioIdAndNombreIgnoreCaseAndIdNot(negocioId, request.nombre(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe una ubicación con ese nombre");
        }

        ubicacion.setNombre(request.nombre());
        ubicacion.setDireccion(request.direccion());
        ubicacion.setTelefono(request.telefono());
        ubicacion.setHorario(request.horario());
        ubicacion.setLatitud(request.latitud());
        ubicacion.setLongitud(request.longitud());
        ubicacion.setRadioMetros(radioO(request.radioMetros()));

        return aResponse(ubicacion);
    }

    @Transactional
    public void eliminar(Long id, Long negocioId) {
        ubicacionRepository.delete(buscar(id, negocioId));
    }

    // Busca siempre por id Y negocio: un negocio nunca accede a sedes de otro
    private Ubicacion buscar(Long id, Long negocioId) {
        return ubicacionRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Ubicación no encontrada"));
    }

    private int radioO(Integer radio) {
        return radio != null ? radio : RADIO_POR_DEFECTO;
    }

    private UbicacionResponse aResponse(Ubicacion u) {
        return new UbicacionResponse(
                u.getId(),
                u.getNombre(),
                u.getDireccion(),
                u.getTelefono(),
                u.getHorario(),
                u.getLatitud(),
                u.getLongitud(),
                u.getRadioMetros(),
                u.getFechaCreacion()
        );
    }
}