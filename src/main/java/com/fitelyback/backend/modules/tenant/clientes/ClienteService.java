package com.fitelyback.backend.modules.tenant.clientes;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.tenant.NegocioRepository;
import com.fitelyback.backend.modules.tenant.clientes.dto.ClienteRequest;
import com.fitelyback.backend.modules.tenant.clientes.dto.ClienteResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final NegocioRepository negocioRepository;

    @Transactional(readOnly = true)
    public List<ClienteResponse> listar(Long negocioId) {
        return clienteRepository.findByNegocioIdOrderByApellidoAscNombreAsc(negocioId)
                .stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtener(Long id, Long negocioId) {
        return aResponse(buscar(id, negocioId));
    }

    @Transactional
    public ClienteResponse crear(ClienteRequest request, Long negocioId) {
        if (clienteRepository.existsByNegocioIdAndTelefono(negocioId, request.telefono())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un cliente con ese teléfono");
        }

        Cliente cliente = Cliente.builder()
                .nombre(request.nombre())
                .apellido(request.apellido())
                .telefono(request.telefono())
                .fechaNacimiento(request.fechaNacimiento())
                .email(request.email())
                .negocio(negocioRepository.getReferenceById(negocioId))
                .build();

        return aResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request, Long negocioId) {
        Cliente cliente = buscar(id, negocioId);

        if (clienteRepository.existsByNegocioIdAndTelefonoAndIdNot(negocioId, request.telefono(), id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un cliente con ese teléfono");
        }

        cliente.setNombre(request.nombre());
        cliente.setApellido(request.apellido());
        cliente.setTelefono(request.telefono());
        cliente.setFechaNacimiento(request.fechaNacimiento());
        cliente.setEmail(request.email());

        return aResponse(cliente);
    }

    @Transactional
    public void eliminar(Long id, Long negocioId) {
        clienteRepository.delete(buscar(id, negocioId));
    }

    // Busca siempre por id Y negocio: un negocio nunca accede a clientes de otro
    private Cliente buscar(Long id, Long negocioId) {
        return clienteRepository.findByIdAndNegocioId(id, negocioId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    private ClienteResponse aResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getTelefono(),
                cliente.getFechaNacimiento(),
                cliente.getEmail(),
                cliente.getFechaRegistro()
        );
    }
}