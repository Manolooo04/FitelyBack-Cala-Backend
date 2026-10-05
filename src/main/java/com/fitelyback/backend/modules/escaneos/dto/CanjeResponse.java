package com.fitelyback.backend.modules.escaneos.dto;

// Resultado del canje: el premio entregado y la tarjeta nueva del cliente
public record CanjeResponse(
        String recompensa,
        String clienteNombre,
        String siguienteTarjetaCodigo,
        String siguienteTarjetaNombre
) {}