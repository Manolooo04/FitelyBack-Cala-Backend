package com.fitelyback.backend.modules.tenant.auth;

import com.fitelyback.backend.modules.tenant.auth.dto.AuthResponse;
import com.fitelyback.backend.modules.tenant.auth.dto.LoginRequest;
import com.fitelyback.backend.modules.tenant.auth.dto.RegistroRequest;
import com.fitelyback.backend.modules.tenant.auth.dto.UserProfileResponse;
import com.fitelyback.backend.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registrar(@RequestBody RegistroRequest request) {
        return ResponseEntity.ok(authService.registrar(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> obtenerMiPerfil(@AuthenticationPrincipal UsuarioAutenticado usuario) {
        return ResponseEntity.ok(authService.obtenerPerfil(usuario.email()));
    }
}