package com.fitelyback.backend.modules.tenant.auth.dto;

import com.fitelyback.backend.modules.tenant.auth.Rol;

public record UserProfileResponse(
        Long usuarioId,
        String email,
        String nombreCompleto,
        Rol rol,
        Long negocioId,
        String nombreNegocio
) {}