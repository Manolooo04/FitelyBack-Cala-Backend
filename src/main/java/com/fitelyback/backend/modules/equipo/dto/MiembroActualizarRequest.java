package com.fitelyback.backend.modules.equipo.dto;

import com.fitelyback.backend.modules.tenant.auth.Rol;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record MiembroActualizarRequest(

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre no puede superar los 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido no puede superar los 50 caracteres")
        String apellido,

        @NotNull(message = "El rol es obligatorio")
        Rol rol,

        // Sedes a las que tendrá acceso. Obligatorio al menos una para STAFF.
        List<Long> ubicacionIds,

        // Opcional: solo se cambia si se envía
        @Size(min = 8, max = 72, message = "La contraseña debe tener entre 8 y 72 caracteres")
        String password
) {}