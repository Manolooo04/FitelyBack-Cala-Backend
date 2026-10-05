package com.fitelyback.backend.modules.escaneos.dto;

// Datos para la pantalla "Confirmar beneficio"
public record RecompensaEscaneadaResponse(
        String codigo,
        String recompensa,
        String clienteNombre,
        String tarjetaNombre
) {}