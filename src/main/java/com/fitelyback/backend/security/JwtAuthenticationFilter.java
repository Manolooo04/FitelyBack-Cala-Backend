package com.fitelyback.backend.security;

import com.fitelyback.backend.modules.tenant.auth.Rol;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        // Sin header o sin "Bearer ": seguimos sin autenticar.
        // Si la ruta es protegida, SecurityConfig responderá 401.
        if (authHeader == null || !authHeader.startsWith(PREFIJO)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(PREFIJO.length()).trim();

        if (jwtService.esTokenValido(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            String rolTexto = jwtService.extraerRol(token);

            // Los tokens antiguos no traen rol: se ignoran y el usuario debe volver a iniciar sesión
            if (rolTexto != null) {
                Rol rol = Rol.valueOf(rolTexto);

                UsuarioAutenticado usuario = new UsuarioAutenticado(
                        jwtService.extraerUsername(token),
                        jwtService.extraerNegocioId(token),
                        rol
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                usuario,
                                null,
                                List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()))
                        );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}