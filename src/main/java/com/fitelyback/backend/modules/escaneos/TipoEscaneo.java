package com.fitelyback.backend.modules.escaneos;

public enum TipoEscaneo {
    SELLO,     // Suma o resta de estampillas
    CANJE,     // Entrega de una recompensa
    VISITA,    // Visita registrada en una tarjeta de niveles
    RECARGA,   // Aumento de saldo en una giftcard
    CONSUMO    // Uso de saldo de una giftcard
}