package com.fitelyback.backend.modules.tarjetas;

import com.fitelyback.backend.modules.tarjetas.dto.EmitirTarjetaRequest;
import com.fitelyback.backend.modules.tarjetas.dto.TarjetaEmitidaResponse;
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
@RequestMapping("/api/v1/tarjetas-emitidas")
@RequiredArgsConstructor
public class TarjetaEmitidaController {

    private final TarjetaEmitidaService tarjetaService;

    // Filtros opcionales: ?ubicacionId=2, ?plantillaId=1, ?clienteId=2
    @GetMapping
    public ResponseEntity<List<TarjetaEmitidaResponse>> listar(
            @RequestParam(required = false) Long ubicacionId,
            @RequestParam(required = false) Long plantillaId,
            @RequestParam(required = false) Long clienteId,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(
                tarjetaService.listar(usuario.negocioId(), ubicacionId, plantillaId, clienteId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TarjetaEmitidaResponse> obtener(@PathVariable Long id,
                                                          @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(tarjetaService.obtener(id, usuario.negocioId()));
    }

    // ADMIN y STAFF pueden emitir tarjetas
    @PostMapping
    public ResponseEntity<TarjetaEmitidaResponse> emitir(@Valid @RequestBody EmitirTarjetaRequest request,
                                                         @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(tarjetaService.emitir(request, usuario.negocioId()));
    }

    // Anular no borra la tarjeta: la deja en estado ANULADA para conservar el historial
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> anular(@PathVariable Long id,
                                       @AuthenticationPrincipal UsuarioAutenticado usuario) {
        tarjetaService.anular(id, usuario.negocioId());
        return ResponseEntity.noContent().build();
    }
}