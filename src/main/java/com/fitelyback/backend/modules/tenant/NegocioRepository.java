package com.fitelyback.backend.modules.tenant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface NegocioRepository extends JpaRepository<Negocio, Long> {
    Optional<Negocio> findByRuc(String ruc);
}