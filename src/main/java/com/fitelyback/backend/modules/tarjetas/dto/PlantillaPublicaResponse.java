package com.fitelyback.backend.modules.tarjetas.dto;

import com.fitelyback.backend.modules.tarjetas.TipoTarjeta;

// Lo que ve el cliente al elegir tienda y tarjeta en la página de registro
public record PlantillaPublicaResponse(
        Long plantillaId,
        TipoTarjeta tipo,
        String nombre,
        Integer metaSellos,
        String recompensa,
        Long ubicacionId,
        String ubicacionNombre
) {}