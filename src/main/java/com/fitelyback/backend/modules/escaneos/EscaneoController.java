package com.fitelyback.backend.modules.escaneos;

import com.fitelyback.backend.modules.escaneos.dto.AbrirEscanerRequest;
import com.fitelyback.backend.modules.escaneos.dto.CanjeResponse;
import com.fitelyback.backend.modules.escaneos.dto.EscanerAbiertoResponse;
import com.fitelyback.backend.modules.escaneos.dto.EstampillasRequest;
import com.fitelyback.backend.modules.escaneos.dto.RecompensaEscaneadaResponse;
import com.fitelyback.backend.modules.escaneos.dto.TarjetaEscaneadaResponse;
import com.fitelyback.backend.security.UsuarioAutenticado;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// Aplicación de escaneo del personal: la usan ADMIN y STAFF
@RestController
@RequestMapping("/api/v1/escaneos")
@RequiredArgsConstructor
public class EscaneoController {

    private final EscaneoService escaneoService;

    // Ingreso con el PIN de la tienda
    @PostMapping("/abrir")
    public ResponseEntity<EscanerAbiertoResponse> abrir(@Valid @RequestBody AbrirEscanerRequest request,
                                                        @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(escaneoService.abrir(request, usuario));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<Void> cerrar(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        escaneoService.cerrar(usuario);
        return ResponseEntity.noContent().build();
    }

    // Escanear QR tarjeta
    @GetMapping("/tarjetas/{codigo}")
    public ResponseEntity<TarjetaEscaneadaResponse> verTarjeta(
            @PathVariable String codigo,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(escaneoService.verTarjeta(codigo, usuario));
    }

    // Agregar o quitar estampillas
    @PostMapping("/tarjetas/{codigo}/estampillas")
    public ResponseEntity<TarjetaEscaneadaResponse> ajustarEstampillas(
            @PathVariable String codigo,
            @Valid @RequestBody EstampillasRequest request,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(escaneoService.ajustarEstampillas(codigo, request, usuario));
    }

    // Escanear QR recompensa: datos para "Confirmar beneficio"
    @GetMapping("/recompensas/{codigo}")
    public ResponseEntity<RecompensaEscaneadaResponse> verRecompensa(
            @PathVariable String codigo,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(escaneoService.verRecompensa(codigo, usuario));
    }

    // Botón "Confirmar": entrega el premio y emite la tarjeta siguiente
    @PostMapping("/recompensas/{codigo}/canjear")
    public ResponseEntity<CanjeResponse> canjearRecompensa(
            @PathVariable String codigo,
            @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(escaneoService.canjearRecompensa(codigo, usuario));
    }
}