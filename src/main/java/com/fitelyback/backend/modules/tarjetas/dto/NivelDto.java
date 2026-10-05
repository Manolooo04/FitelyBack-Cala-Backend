package com.fitelyback.backend.modules.tarjetas.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record NivelDto(

        @NotBlank(message = "El nombre del nivel es obligatorio")
        @Size(max = 50, message = "El nombre del nivel no puede superar los 50 caracteres")
        String nombre,

        @NotNull(message = "El mínimo de visitas es obligatorio")
        @Min(value = 0, message = "El mínimo de visitas no puede ser negativo")
        Integer visitasMinimas,

        @Size(max = 150, message = "El beneficio no puede superar los 150 caracteres")
        String beneficio
) {}