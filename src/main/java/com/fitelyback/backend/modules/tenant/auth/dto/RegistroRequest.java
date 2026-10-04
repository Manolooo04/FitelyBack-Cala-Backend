package com.fitelyback.backend.modules.tenant.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegistroRequest {
    private String nombreEmpresa;
    private String nombre;
    private String apellido;
    private String email;
    private String password;
}