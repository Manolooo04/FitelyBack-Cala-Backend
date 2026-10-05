package com.fitelyback.backend.modules.escaneos.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record EstampillasRequest(

        // Positivo para sumar, negativo para restar
        @NotNull(message = "La cantidad es obligatoria")
        @Min(value = -20, message = "No se pueden quitar más de 20 estampillas a la vez")
        @Max(value = 20, message = "No se pueden agregar más de 20 estampillas a la vez")
        Integer cantidad
) {}