package com.fitelyback.backend.modules.tenant.clientes;

import com.fitelyback.backend.modules.tenant.Negocio;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "clientes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_clientes_negocio_telefono",
                columnNames = {"negocio_id", "telefono"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, length = 20)
    private String telefono;

    // Opcional: el registro del cliente no lo pide
    @Column(length = 100)
    private String email;

    private LocalDate fechaNacimiento;

    @Column(updatable = false)
    private LocalDateTime fechaRegistro;

    // Cada cliente pertenece a un único negocio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @PrePersist
    protected void onCreate() {
        this.fechaRegistro = LocalDateTime.now();
    }
}