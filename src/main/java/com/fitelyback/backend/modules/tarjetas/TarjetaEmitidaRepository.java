package com.fitelyback.backend.modules.tarjetas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TarjetaEmitidaRepository extends JpaRepository<TarjetaEmitida, Long> {

    List<TarjetaEmitida> findByNegocioIdOrderByFechaEmisionDesc(Long negocioId);

    Optional<TarjetaEmitida> findByIdAndNegocioId(Long id, Long negocioId);

    Optional<TarjetaEmitida> findByCodigo(String codigo);

    boolean existsByClienteIdAndPlantillaIdAndEstado(Long clienteId, Long plantillaId, EstadoTarjeta estado);

    long countByClienteIdAndEstado(Long clienteId, EstadoTarjeta estado);
}