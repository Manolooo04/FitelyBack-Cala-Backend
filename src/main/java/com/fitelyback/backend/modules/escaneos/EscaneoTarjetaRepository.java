package com.fitelyback.backend.modules.escaneos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EscaneoTarjetaRepository extends JpaRepository<EscaneoTarjeta, Long> {

    List<EscaneoTarjeta> findTop20ByTarjetaIdOrderByFechaDesc(Long tarjetaId);
}