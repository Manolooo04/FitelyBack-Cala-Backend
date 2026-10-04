package com.fitelyback.backend.security;

import com.fitelyback.backend.modules.tenant.auth.Rol;

public record UsuarioAutenticado(String email, Long negocioId, Rol rol) {}