package com.fitelyback.backend.modules.tarjetas.dto;

import com.fitelyback.backend.modules.tarjetas.EstadoTarjeta;
import com.fitelyback.backend.modules.tarjetas.TipoTarjeta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TarjetaEmitidaResponse(
        Long id,
        String codigo,
        EstadoTarjeta estado,
        boolean vencida,
        TipoTarjeta tipo,
        Long plantillaId,
        String plantillaNombre,
        Long ubicacionId,
        String ubicacionNombre,
        Long clienteId,
        String clienteNombre,
        String clienteTelefono,
        int sellos,
        Integer metaSellos,
        boolean completa,
        String recompensa,
        int visitas,
        String nivelActual,
        BigDecimal saldo,
        long canjes,
        LocalDateTime fechaEmision,
        LocalDate fechaVencimiento
) {}