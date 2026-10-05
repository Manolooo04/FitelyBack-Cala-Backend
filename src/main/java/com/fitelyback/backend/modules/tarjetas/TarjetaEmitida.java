package com.fitelyback.backend.modules.tarjetas;

import com.fitelyback.backend.modules.tenant.Negocio;
import com.fitelyback.backend.modules.tenant.clientes.Cliente;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tarjetas_emitidas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaEmitida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código único que va dentro del QR de la tarjeta
    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTarjeta estado;

    // Progreso según el tipo de tarjeta
    @Column(nullable = false)
    private int sellos;

    @Column(nullable = false)
    private int visitas;

    @Column(precision = 10, scale = 2)
    private BigDecimal saldo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaEmision;

    @Column(nullable = false)
    private LocalDate fechaVencimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plantilla_id", nullable = false)
    private TarjetaPlantilla plantilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;
}