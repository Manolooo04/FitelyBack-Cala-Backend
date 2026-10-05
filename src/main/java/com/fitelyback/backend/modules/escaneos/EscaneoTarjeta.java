package com.fitelyback.backend.modules.escaneos;

import com.fitelyback.backend.modules.tarjetas.TarjetaEmitida;
import com.fitelyback.backend.modules.tenant.Negocio;
import com.fitelyback.backend.modules.tenant.auth.Usuario;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Registro inmutable de cada movimiento hecho por el personal sobre una tarjeta
@Entity
@Table(name = "escaneos_tarjeta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EscaneoTarjeta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoEscaneo tipo;

    // Estampillas o visitas sumadas (positivo) o restadas (negativo)
    private Integer cantidad;

    private Integer valorAntes;

    private Integer valorDespues;

    // Gasto estimado del movimiento, o monto recargado o consumido
    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fecha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarjeta_id", nullable = false)
    private TarjetaEmitida tarjeta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @PrePersist
    protected void onCreate() {
        this.fecha = LocalDateTime.now();
    }
}