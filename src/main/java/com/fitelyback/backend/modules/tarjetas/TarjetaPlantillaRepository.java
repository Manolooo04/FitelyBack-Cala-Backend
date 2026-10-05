package com.fitelyback.backend.modules.tarjetas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarjetaPlantillaRepository extends JpaRepository<TarjetaPlantilla, Long> {

    List<TarjetaPlantilla> findByNegocioIdOrderByNombreAsc(Long negocioId);

    List<TarjetaPlantilla> findByNegocioIdAndUbicacionIdOrderByNombreAsc(Long negocioId, Long ubicacionId);

    Optional<TarjetaPlantilla> findByIdAndNegocioId(Long id, Long negocioId);

    boolean existsByUbicacionIdAndNombreIgnoreCase(Long ubicacionId, String nombre);

    boolean existsByUbicacionIdAndNombreIgnoreCaseAndIdNot(Long ubicacionId, String nombre, Long id);
}