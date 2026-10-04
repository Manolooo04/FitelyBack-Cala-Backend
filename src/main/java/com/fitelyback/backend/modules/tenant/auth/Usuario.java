package com.fitelyback.backend.modules.tenant.auth;

import com.fitelyback.backend.modules.tenant.Negocio;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String apellido;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Es nullable porque si el usuario entra con Google/Apple, no tendrá contraseña en nuestra base de datos.
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProveedorAuth proveedor;

    // Cada usuario pertenece a un negocio (La empresa que registró en el formulario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;
}