package com.fitelyback.backend.modules.ubicaciones;

import com.fitelyback.backend.modules.ubicaciones.dto.UbicacionRequest;
import com.fitelyback.backend.modules.ubicaciones.dto.UbicacionResponse;
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
@RequestMapping("/api/v1/ubicaciones")
@RequiredArgsConstructor
public class UbicacionController {

    private final UbicacionService ubicacionService;

    // ADMIN y STAFF: alimenta el selector de sede de la cabecera
    @GetMapping
    public ResponseEntity<List<UbicacionResponse>> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(ubicacionService.listar(usuario.negocioId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UbicacionResponse> obtener(@PathVariable Long id,
                                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(ubicacionService.obtener(id, usuario.negocioId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UbicacionResponse> crear(@Valid @RequestBody UbicacionRequest request,
                                                   @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ubicacionService.crear(request, usuario.negocioId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UbicacionResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody UbicacionRequest request,
                                                        @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(ubicacionService.actualizar(id, request, usuario.negocioId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado usuario) {
        ubicacionService.eliminar(id, usuario.negocioId());
        return ResponseEntity.noContent().build();
    }
}