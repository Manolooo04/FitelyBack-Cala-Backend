package com.fitelyback.backend.modules.recompensas;

import com.fitelyback.backend.modules.recompensas.dto.RecompensaPublicaResponse;
import com.fitelyback.backend.modules.tarjetas.dto.TarjetaPublicaResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// Endpoints sin token: los usa el cliente desde la página de su tarjeta
@RestController
@RequestMapping("/api/v1/publico/tarjetas")
@RequiredArgsConstructor
public class RecompensaPublicaController {

    private final RecompensaService recompensaService;

    // Botón "Canjear": código para mostrar el QR de recompensa
    @GetMapping("/{codigo}/recompensa")
    public ResponseEntity<RecompensaPublicaResponse> recompensa(@PathVariable String codigo) {
        return ResponseEntity.ok(recompensaService.pendienteDeTarjeta(codigo));
    }

    // Botón "Mostrar siguiente QR": la tarjeta nueva tras el canje
    @GetMapping("/{codigo}/siguiente")
    public ResponseEntity<TarjetaPublicaResponse> siguiente(@PathVariable String codigo) {
        return ResponseEntity.ok(recompensaService.siguienteDeTarjeta(codigo));
    }
}