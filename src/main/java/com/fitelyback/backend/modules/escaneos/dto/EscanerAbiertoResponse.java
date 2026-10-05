package com.fitelyback.backend.modules.escaneos.dto;

import java.time.LocalDateTime;

public record EscanerAbiertoResponse(
        Long ubicacionId,
        String ubicacionNombre,
        LocalDateTime abiertoHasta
) {}