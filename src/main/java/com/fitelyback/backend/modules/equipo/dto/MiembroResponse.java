package com.fitelyback.backend.modules.equipo.dto;

import com.fitelyback.backend.modules.tenant.auth.Rol;

import java.util.List;

public record MiembroResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        Rol rol,
        Long ubicacionId,
        String tiendaActual,
        List<String> accesoTiendas,
        boolean tienePin,
        boolean esTuCuenta
) {}