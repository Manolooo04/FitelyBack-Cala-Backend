package com.fitelyback.backend.modules.tarjetas;

import com.fitelyback.backend.modules.tarjetas.dto.PlantillaRequest;
import com.fitelyback.backend.modules.tarjetas.dto.PlantillaResponse;
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
@RequestMapping("/api/v1/tarjetas-plantilla")
@RequiredArgsConstructor
public class TarjetaPlantillaController {

    private final TarjetaPlantillaService plantillaService;

    // Filtros opcionales: ?ubicacionId=1 y ?tipo=ESTAMPILLAS
    @GetMapping
    public ResponseEntity<List<PlantillaResponse>> listar(
            @RequestParam(required = false) Long ubicacionId,
            @RequestParam(required = false) TipoTarjeta tipo,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(plantillaService.listar(usuario.negocioId(), ubicacionId, tipo));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlantillaResponse> obtener(@PathVariable Long id,
                                                     @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(plantillaService.obtener(id, usuario.negocioId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<PlantillaResponse> crear(@Valid @RequestBody PlantillaRequest request,
                                                   @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(plantillaService.crear(request, usuario.negocioId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PlantillaResponse> actualizar(@PathVariable Long id,
                                                        @Valid @RequestBody PlantillaRequest request,
                                                        @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(plantillaService.actualizar(id, request, usuario.negocioId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal UsuarioAutenticado usuario) {
        plantillaService.eliminar(id, usuario.negocioId());
        return ResponseEntity.noContent().build();
    }
}