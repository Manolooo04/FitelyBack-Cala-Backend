package com.fitelyback.backend.modules.recompensas;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RecompensaRepository extends JpaRepository<Recompensa, Long> {

    Optional<Recompensa> findByCodigo(String codigo);

    Optional<Recompensa> findByTarjetaId(Long tarjetaId);
}