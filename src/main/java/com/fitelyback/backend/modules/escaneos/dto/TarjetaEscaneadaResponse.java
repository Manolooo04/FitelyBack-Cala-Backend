package com.fitelyback.backend.modules.escaneos.dto;

import com.fitelyback.backend.modules.tarjetas.dto.TarjetaEmitidaResponse;

import java.util.List;

// Lo que ve el trabajador al escanear: la tarjeta y sus últimos movimientos
public record TarjetaEscaneadaResponse(
        TarjetaEmitidaResponse tarjeta,
        List<EscaneoResponse> historial
) {}