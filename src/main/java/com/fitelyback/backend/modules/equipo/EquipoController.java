package com.fitelyback.backend.modules.equipo;

import com.fitelyback.backend.modules.equipo.dto.MiembroActualizarRequest;
import com.fitelyback.backend.modules.equipo.dto.MiembroCrearRequest;
import com.fitelyback.backend.modules.equipo.dto.MiembroResponse;
import com.fitelyback.backend.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Todo el módulo es exclusivo del dueño del negocio
@RestController
@RequestMapping("/api/v1/equipo")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EquipoController {

    private final EquipoService equipoService;

    @GetMapping
    public ResponseEntity<List<MiembroResponse>> listar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(equipoService.listar(usuario));
    }

    @PostMapping
    public ResponseEntity<MiembroResponse> crear(@Valid @RequestBody MiembroCrearRequest request,
                                                 @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipoService.crear(request, usuario));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MiembroResponse> actualizar(@PathVariable Long id,
                                                      @Valid @RequestBody MiembroActualizarRequest request,
                                                      @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(equipoService.actualizar(id, request, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado usuario) {
        equipoService.eliminar(id, usuario);
        return ResponseEntity.noContent().build();
    }
}