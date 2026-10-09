package com.fitelyback.backend.modules.clientes.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nombre,
        String apellido,
        String telefono,
        LocalDate fechaNacimiento,
        String email,
        LocalDateTime fechaRegistro
) {}