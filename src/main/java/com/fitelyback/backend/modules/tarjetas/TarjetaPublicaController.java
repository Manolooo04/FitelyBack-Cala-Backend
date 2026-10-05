package com.fitelyback.backend.modules.tarjetas;

import com.fitelyback.backend.modules.tarjetas.dto.PlantillaPublicaResponse;
import com.fitelyback.backend.modules.tarjetas.dto.RegistroClienteRequest;
import com.fitelyback.backend.modules.tarjetas.dto.TarjetaPublicaResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Endpoints sin token: los usa el cliente final desde su teléfono
@RestController
@RequestMapping("/api/v1/publico")
@RequiredArgsConstructor
public class TarjetaPublicaController {

    private final TarjetaEmitidaService tarjetaService;

    // Tiendas y tarjetas que el cliente puede elegir en "Crear mi tarjeta"
    @GetMapping("/negocios/{negocioId}/tarjetas")
    public ResponseEntity<List<PlantillaPublicaResponse>> tarjetasDisponibles(@PathVariable Long negocioId) {
        return ResponseEntity.ok(tarjetaService.plantillasPublicas(negocioId));
    }

    @PostMapping("/negocios/{negocioId}/registro")
    public ResponseEntity<TarjetaPublicaResponse> registrar(@PathVariable Long negocioId,
                                                            @Valid @RequestBody RegistroClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(tarjetaService.registrar(negocioId, request));
    }

    // Página de la tarjeta del cliente
    @GetMapping("/tarjetas/{codigo}")
    public ResponseEntity<TarjetaPublicaResponse> verTarjeta(@PathVariable String codigo) {
        return ResponseEntity.ok(tarjetaService.verPorCodigo(codigo));
    }
}