package com.fitelyback.backend.modules.tenant.auth;

import com.fitelyback.backend.modules.tenant.Negocio;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.LinkedHashSet;
import java.util.Set;

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

    // ADMIN: dueño del negocio. STAFF: trabajador invitado.
    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ADMIN'")
    @Column(nullable = false, length = 20)
    private Rol rol;

    // Tienda actual: la sede donde el usuario está operando
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    // Sedes a las que el usuario tiene acceso. Un ADMIN accede a todas aunque la lista esté vacía.
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_ubicaciones",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "ubicacion_id")
    )
    @Builder.Default
    private Set<Ubicacion> ubicaciones = new LinkedHashSet<>();

    // Cada usuario pertenece a un negocio (La empresa que registró en el formulario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;
}