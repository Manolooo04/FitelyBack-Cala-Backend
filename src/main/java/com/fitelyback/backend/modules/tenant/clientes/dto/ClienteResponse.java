package com.fitelyback.backend.modules.tenant.clientes.dto;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String nombre,
        String apellido,
        String telefono,
        String email,
        LocalDateTime fechaRegistro
) {}