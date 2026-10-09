package com.fitelyback.backend.modules.clientes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    List<Cliente> findByNegocioIdOrderByApellidoAscNombreAsc(Long negocioId);

    Optional<Cliente> findByIdAndNegocioId(Long id, Long negocioId);

    Optional<Cliente> findByNegocioIdAndTelefono(Long negocioId, String telefono);

    boolean existsByNegocioIdAndTelefono(Long negocioId, String telefono);

    boolean existsByNegocioIdAndTelefonoAndIdNot(Long negocioId, String telefono, Long id);
}