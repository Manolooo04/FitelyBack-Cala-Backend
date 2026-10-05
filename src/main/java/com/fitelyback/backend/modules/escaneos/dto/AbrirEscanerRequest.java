package com.fitelyback.backend.modules.escaneos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AbrirEscanerRequest(

        @NotNull(message = "La sede es obligatoria")
        Long ubicacionId,

        @NotBlank(message = "El PIN es obligatorio")
        String pin
) {}