package com.fitelyback.backend.modules.equipo.dto;

import com.fitelyback.backend.modules.tenant.auth.Rol;

import java.util.List;

public record MiembroResponse(
        Long id,
        String nombre,
        String apellido,
        String email,
        Rol rol,
        String tiendaActual,
        List<Long> ubicacionIds,
        List<String> accesoTiendas,
        boolean esTuCuenta
) {}