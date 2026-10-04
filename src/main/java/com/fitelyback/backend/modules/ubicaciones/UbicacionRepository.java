package com.fitelyback.backend.modules.ubicaciones;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {

    List<Ubicacion> findByNegocioIdOrderByNombreAsc(Long negocioId);

    Optional<Ubicacion> findByIdAndNegocioId(Long id, Long negocioId);

    boolean existsByNegocioIdAndNombreIgnoreCase(Long negocioId, String nombre);

    boolean existsByNegocioIdAndNombreIgnoreCaseAndIdNot(Long negocioId, String nombre, Long id);
}