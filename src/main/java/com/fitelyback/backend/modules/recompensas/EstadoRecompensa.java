package com.fitelyback.backend.modules.recompensas;

public enum EstadoRecompensa {
    PENDIENTE,  // Disponible para canjear en tienda
    CANJEADA    // Ya entregada; el código no se puede volver a usar
}