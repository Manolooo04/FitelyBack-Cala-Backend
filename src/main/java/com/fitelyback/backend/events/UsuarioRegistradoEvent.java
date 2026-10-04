package com.fitelyback.backend.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UsuarioRegistradoEvent {
    private String email;
    private Long negocioId;
}