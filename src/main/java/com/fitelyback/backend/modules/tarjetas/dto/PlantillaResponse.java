package com.fitelyback.backend.modules.tarjetas.dto;

import com.fitelyback.backend.modules.tarjetas.TipoTarjeta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record PlantillaResponse(
        Long id,
        TipoTarjeta tipo,
        String nombre,
        Long ubicacionId,
        String ubicacionNombre,
        boolean activa,
        BigDecimal precioProducto,
        String colorFondo,
        String colorTexto,
        String logoUrl,
        String imagenFondoUrl,
        String iconoSelloActivo,
        String iconoSelloInactivo,
        String descripcion,
        String telefono,
        String sitioWeb,
        Integer metaSellos,
        String recompensa,
        Long siguientePlantillaId,
        String siguientePlantillaNombre,
        List<NivelDto> niveles,
        BigDecimal saldoInicial,
        LocalDateTime fechaCreacion
) {}