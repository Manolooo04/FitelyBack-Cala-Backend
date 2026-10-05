package com.fitelyback.backend.modules.recompensas;

import com.fitelyback.backend.modules.tarjetas.TarjetaEmitida;
import com.fitelyback.backend.modules.tenant.Negocio;
import com.fitelyback.backend.modules.tenant.auth.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

// Premio ganado al completar una tarjeta. Su código va en el QR de recompensa y sirve una sola vez.
@Entity
@Table(name = "recompensas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recompensa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoRecompensa estado;

    // Nombre del premio al momento de ganarlo (por ejemplo "Vaso Classic")
    @Column(nullable = false, length = 150)
    private String descripcion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaGeneracion;

    private LocalDateTime fechaCanje;

    // Tarjeta que se completó para ganar este premio
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarjeta_id", nullable = false, unique = true)
    private TarjetaEmitida tarjeta;

    // Tarjeta nueva que recibió el cliente al canjear
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarjeta_siguiente_id")
    private TarjetaEmitida tarjetaSiguiente;

    // Trabajador que entregó el premio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "canjeado_por_id")
    private Usuario canjeadoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @PrePersist
    protected void onCreate() {
        this.fechaGeneracion = LocalDateTime.now();
    }
}