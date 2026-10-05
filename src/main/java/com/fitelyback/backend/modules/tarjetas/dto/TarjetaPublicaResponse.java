package com.fitelyback.backend.modules.tarjetas.dto;

import com.fitelyback.backend.modules.tarjetas.EstadoTarjeta;
import com.fitelyback.backend.modules.tarjetas.TipoTarjeta;

import java.math.BigDecimal;
import java.time.LocalDate;

// La tarjeta tal como la ve el cliente: progreso y diseño, sin datos personales sensibles
public record TarjetaPublicaResponse(
        String codigo,
        EstadoTarjeta estado,
        boolean vencida,
        TipoTarjeta tipo,
        String plantillaNombre,
        String ubicacionNombre,
        String clienteNombre,
        int sellos,
        Integer metaSellos,
        boolean completa,
        String recompensa,
        int visitas,
        String nivelActual,
        BigDecimal saldo,
        long canjes,
        LocalDate fechaVencimiento,
        String colorFondo,
        String colorTexto,
        String logoUrl,
        String imagenFondoUrl,
        String iconoSelloActivo,
        String iconoSelloInactivo,
        String descripcion,
        String telefono,
        String sitioWeb
) {}