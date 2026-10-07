package com.fitelyback.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

// Frena los intentos repetidos en el login, el PIN de tienda y el registro público
@Component
@RequiredArgsConstructor
public class LimiteIntentosFilter extends OncePerRequestFilter {

    private static final Duration QUINCE_MINUTOS = Duration.ofMinutes(15);
    private static final Duration UNA_HORA = Duration.ofHours(1);

    private final LimitadorIntentos limitador;

    // soloFallos = true: solo cuentan las respuestas con error, y un acierto borra la cuenta
    private record Regla(String clave, int maximo, Duration ventana, boolean soloFallos) {}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Regla regla = reglaPara(request);
        if (regla == null) {
            chain.doFilter(request, response);
            return;
        }

        long espera = limitador.segundosBloqueado(regla.clave());
        if (espera > 0) {
            responderBloqueo(request, response, espera);
            return;
        }

        chain.doFilter(request, response);

        boolean fallo = response.getStatus() >= 400;
        if (!regla.soloFallos() || fallo) {
            limitador.registrarIntento(regla.clave(), regla.maximo(), regla.ventana());
        } else {
            limitador.limpiar(regla.clave());
        }
    }

    private Regla reglaPara(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String ruta = request.getRequestURI();
        String ip = request.getRemoteAddr();

        if (ruta.equals("/api/v1/auth/login")) {
            return new Regla("login:" + ip, 5, QUINCE_MINUTOS, true);
        }
        if (ruta.equals("/api/v1/escaneos/abrir")) {
            return new Regla("pin:" + usuarioActual(ip), 5, QUINCE_MINUTOS, true);
        }
        if (ruta.matches("/api/v1/publico/negocios/\\d+/registro")) {
            return new Regla("registro:" + ip, 10, UNA_HORA, false);
        }
        return null;
    }

    // Correo del usuario con sesión iniciada; si no hay, se usa la IP
    private String usuarioActual(String ip) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UsuarioAutenticado usuario) {
            return usuario.email();
        }
        return ip;
    }

    private void responderBloqueo(HttpServletRequest request, HttpServletResponse response,
                                  long segundos) throws IOException {
        long minutos = Math.max((segundos + 59) / 60, 1);
        String mensaje = "Demasiados intentos. Vuelve a intentarlo en " + minutos
                + (minutos == 1 ? " minuto." : " minutos.");

        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(String.format(
                "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\","
                        + "\"message\":\"%s\",\"path\":\"%s\",\"fieldErrors\":{}}",
                LocalDateTime.now(), mensaje, request.getRequestURI()));
    }
}