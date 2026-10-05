package com.fitelyback.backend.modules.escaneos.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SaldoRequest(

        // Positivo para recargar, negativo para consumir
        @NotNull(message = "El monto es obligatorio")
        @Digits(integer = 8, fraction = 2, message = "El monto no es válido")
        BigDecimal monto
) {}