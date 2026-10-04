package com.fitelyback.backend.modules.tenant.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByEmail(String email);
    boolean existsByEmail(String email);

    List<Usuario> findByNegocioIdOrderByNombreAscApellidoAsc(Long negocioId);
    Optional<Usuario> findByIdAndNegocioId(Long id, Long negocioId);
}