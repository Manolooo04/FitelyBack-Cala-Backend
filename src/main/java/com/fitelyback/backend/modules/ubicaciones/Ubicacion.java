package com.fitelyback.backend.modules.ubicaciones;

import com.fitelyback.backend.modules.tenant.Negocio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ubicaciones",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ubicaciones_negocio_nombre",
                columnNames = {"negocio_id", "nombre"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String horario;

    // Coordenadas de la sede, usadas por la geolocalización
    private Double latitud;

    private Double longitud;

    // Radio de detección de la geovalla, en metros
    @Column(nullable = false)
    private Integer radioMetros;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}