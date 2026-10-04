package com.fitelyback.backend.modules.tenant.auth.dto;

public record UserProfileResponse(
        Long usuarioId,
        String email,
        String nombreCompleto,
        Long negocioId,
        String nombreNegocio
) {}