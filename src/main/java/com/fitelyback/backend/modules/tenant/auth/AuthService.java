package com.fitelyback.backend.modules.tenant.auth;

import com.fitelyback.backend.exception.ApiException;
import com.fitelyback.backend.modules.tenant.Negocio;
import com.fitelyback.backend.modules.tenant.NegocioRepository;
import com.fitelyback.backend.modules.tenant.auth.dto.AuthResponse;
import com.fitelyback.backend.modules.tenant.auth.dto.LoginRequest;
import com.fitelyback.backend.modules.tenant.auth.dto.RegistroRequest;
import com.fitelyback.backend.modules.tenant.auth.dto.UserProfileResponse;
import com.fitelyback.backend.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final NegocioRepository negocioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse registrar(RegistroRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ApiException(HttpStatus.CONFLICT, "El correo ya se encuentra registrado");
        }

        // 1. Crear y guardar el negocio matriz
        Negocio negocio = Negocio.builder()
                .nombreComercial(request.getNombreEmpresa())
                .activo(true)
                .build();
        negocio = negocioRepository.save(negocio);

        // 2. Crear y guardar el usuario administrador con clave encriptada en BCrypt
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .apellido(request.getApellido())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .proveedor(ProveedorAuth.LOCAL)
                .negocio(negocio)
                .build();
        usuarioRepository.save(usuario);

        // 3. Generar token JWT
        String token = jwtService.generarToken(usuario.getEmail(), negocio.getId());

        return AuthResponse.builder()
                .token(token)
                .negocioId(negocio.getId())
                .nombreNegocio(negocio.getNombreComercial())
                .email(usuario.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas");
        }

        String token = jwtService.generarToken(usuario.getEmail(), usuario.getNegocio().getId());

        return AuthResponse.builder()
                .token(token)
                .negocioId(usuario.getNegocio().getId())
                .nombreNegocio(usuario.getNegocio().getNombreComercial())
                .email(usuario.getEmail())
                .build();
    }

    @Transactional(readOnly = true)
    public UserProfileResponse obtenerPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        Negocio negocio = usuario.getNegocio();

        return new UserProfileResponse(
                usuario.getId(),
                usuario.getEmail(),
                usuario.getNombre() + " " + usuario.getApellido(),
                negocio.getId(),
                negocio.getNombreComercial()
        );
    }
}