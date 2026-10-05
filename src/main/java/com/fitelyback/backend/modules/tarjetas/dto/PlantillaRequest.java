package com.fitelyback.backend.modules.tarjetas.dto;

import com.fitelyback.backend.modules.tarjetas.TipoTarjeta;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public record PlantillaRequest(

        @NotNull(message = "El tipo de tarjeta es obligatorio")
        TipoTarjeta tipo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar los 100 caracteres")
        String nombre,

        @NotNull(message = "La sede es obligatoria")
        Long ubicacionId,

        Boolean activa,

        // Precio del producto asociado, para estimar el gasto del cliente
        @DecimalMin(value = "0.0", message = "El precio no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El precio admite hasta 2 decimales")
        BigDecimal precioProducto,

        // Diseño visual
        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color de fondo debe tener el formato #RRGGBB")
        String colorFondo,

        @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "El color de texto debe tener el formato #RRGGBB")
        String colorTexto,

        @Size(max = 255, message = "La URL del logo no puede superar los 255 caracteres")
        String logoUrl,

        @Size(max = 255, message = "La URL de la imagen de fondo no puede superar los 255 caracteres")
        String imagenFondoUrl,

        @Size(max = 255, message = "El ícono del sello activo no puede superar los 255 caracteres")
        String iconoSelloActivo,

        @Size(max = 255, message = "El ícono del sello inactivo no puede superar los 255 caracteres")
        String iconoSelloInactivo,

        // Reverso de la tarjeta
        @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
        String descripcion,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String telefono,

        @Size(max = 150, message = "El sitio web no puede superar los 150 caracteres")
        String sitioWeb,

        // Solo ESTAMPILLAS
        @Min(value = 2, message = "La meta mínima es de 2 sellos")
        @Max(value = 20, message = "La meta máxima es de 20 sellos")
        Integer metaSellos,

        @Size(max = 150, message = "La recompensa no puede superar los 150 caracteres")
        String recompensa,

        // Tarjeta a la que pasa el cliente tras canjear; vacío para repetir la misma
        Long siguientePlantillaId,

        // Solo NIVELES
        @Valid
        List<NivelDto> niveles,

        // Solo GIFTCARD
        @DecimalMin(value = "0.0", message = "El saldo inicial no puede ser negativo")
        @Digits(integer = 8, fraction = 2, message = "El saldo inicial admite hasta 2 decimales")
        BigDecimal saldoInicial
) {}