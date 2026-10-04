package com.fitelyback.backend.modules.equipo.dto;

import com.fitelyback.backend.modules.tenant.auth.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record MiembroActualizarRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido no puede superar los 50 caracteres")
        String apellido,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        Long ubicacionId,

        // Opcional: solo se cambia si se envía
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password,

        // Opcional: solo se cambia si se envía
        @Pattern(regexp = "\\d{4,6}", message = "El PIN debe tener entre 4 y 6 dígitos")
        String pin
) {}