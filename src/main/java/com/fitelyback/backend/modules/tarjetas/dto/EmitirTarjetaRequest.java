package com.fitelyback.backend.modules.tarjetas.dto;

import jakarta.validation.constraints.NotNull;

public record EmitirTarjetaRequest(

        @NotNull(message = "El cliente es obligatorio")
        Long clienteId,

        @NotNull(message = "La tarjeta es obligatoria")
        Long plantillaId
) {}