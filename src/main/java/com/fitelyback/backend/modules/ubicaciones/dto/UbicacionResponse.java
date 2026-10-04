package com.fitelyback.backend.modules.ubicaciones.dto;

import java.time.LocalDateTime;

public record UbicacionResponse(
        Long id,
        String nombre,
        String direccion,
        String telefono,
        String horario,
        Double latitud,
        Double longitud,
        Integer radioMetros,
        LocalDateTime fechaCreacion
) {}