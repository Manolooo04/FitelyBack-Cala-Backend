package com.fitelyback.backend.modules.tenant;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "negocios") // Nombre supabase
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Negocio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, length = 100)
    private String nombreComercial;

    @Column(unique = true, length = 20)
    private String ruc;

    private String logoUrl;

    // @Builder.Default hace que Negocio.builder() respete el valor inicial "true"
    @Builder.Default
    @Column(nullable = false)
    private boolean activo = true;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}