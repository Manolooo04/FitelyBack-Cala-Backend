package com.fitelyback.backend.modules.escaneos.dto;

import com.fitelyback.backend.modules.escaneos.TipoEscaneo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EscaneoResponse(
        Long id,
        TipoEscaneo tipo,
        Integer cantidad,
        Integer valorAntes,
        Integer valorDespues,
        BigDecimal monto,
        String usuarioNombre,
        String ubicacionNombre,
        LocalDateTime fecha
) {}