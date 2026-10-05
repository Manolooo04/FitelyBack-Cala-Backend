package com.fitelyback.backend.modules.tarjetas;

public enum EstadoTarjeta {
    ACTIVA,    // En uso: acumula sellos, visitas o saldo
    CANJEADA,  // Se completó y el premio ya fue entregado
    ANULADA    // Dada de baja por el negocio
}