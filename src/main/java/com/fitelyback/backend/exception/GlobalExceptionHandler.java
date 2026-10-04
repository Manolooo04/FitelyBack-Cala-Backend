package com.fitelyback.backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Errores de negocio: cada uno trae su propio código (401, 404, 409...)
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex,
                                                                  HttpServletRequest request) {
        return responder(ex.getStatus(), ex.getMessage(), request, Map.of());
    }

    // Datos que no pasan las validaciones (@NotBlank, @Email, @Size)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError campo : ex.getBindingResult().getFieldErrors()) {
            errores.putIfAbsent(campo.getField(), campo.getDefaultMessage());
        }
        return responder(HttpStatus.BAD_REQUEST, "Datos inválidos", request, errores);
    }

    // JSON mal formado o body vacío
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonInvalido(HttpMessageNotReadableException ex,
                                                                  HttpServletRequest request) {
        return responder(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido",
                request, Map.of());
    }

    // El usuario está autenticado pero su rol no tiene permiso
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccesoDenegado(AccessDeniedException ex,
                                                                    HttpServletRequest request) {
        return responder(HttpStatus.FORBIDDEN, "No tienes permiso para realizar esta acción",
                request, Map.of());
    }

    // La base de datos rechazó el cambio por una restricción (dato duplicado o referencia en uso)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleIntegridad(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        log.warn("Violación de integridad en {}: {}", request.getRequestURI(),
                ex.getMostSpecificCause().getMessage());
        return responder(HttpStatus.CONFLICT,
                "La operación entra en conflicto con datos existentes", request, Map.of());
    }

    // Cualquier error inesperado: se registra en el log y no se expone el detalle
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex,
                                                                      HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor",
                request, Map.of());
    }

    private ResponseEntity<Map<String, Object>> responder(HttpStatus status,
                                                          String mensaje,
                                                          HttpServletRequest request,
                                                          Map<String, String> fieldErrors) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", mensaje);
        body.put("path", request.getRequestURI());
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(status).body(body);
    }
}