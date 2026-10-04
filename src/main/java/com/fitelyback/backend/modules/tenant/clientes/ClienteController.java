package com.fitelyback.backend.modules.tenant.clientes;

import com.fitelyback.backend.modules.tenant.clientes.dto.ClienteRequest;
import com.fitelyback.backend.modules.tenant.clientes.dto.ClienteResponse;
import com.fitelyback.backend.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(clienteService.listar(usuario.negocioId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteResponse> obtener(@PathVariable Long id,
                                                   @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(clienteService.obtener(id, usuario.negocioId()));
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> crear(@Valid @RequestBody ClienteRequest request,
                                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(clienteService.crear(request, usuario.negocioId()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody ClienteRequest request,
                                                      @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(clienteService.actualizar(id, request, usuario.negocioId()));
    }

    // Solo el dueño del negocio puede eliminar clientes
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado usuario) {
        clienteService.eliminar(id, usuario.negocioId());
        return ResponseEntity.noContent().build();
    }
}