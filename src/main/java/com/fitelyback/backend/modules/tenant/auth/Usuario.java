package com.fitelyback.backend.modules.tenant.auth;

import com.fitelyback.backend.modules.tenant.Negocio;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

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

    // PIN cifrado para la interfaz de escaneo en caja (opcional)
    private String pin;

    // Sede asignada. Obligatoria para STAFF; un ADMIN puede no tenerla.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    // Cada usuario pertenece a un negocio (La empresa que registró en el formulario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;
}