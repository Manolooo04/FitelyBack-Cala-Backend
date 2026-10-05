package com.fitelyback.backend.modules.tarjetas;

import com.fitelyback.backend.modules.tenant.Negocio;
import com.fitelyback.backend.modules.ubicaciones.Ubicacion;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "tarjetas_plantilla",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_plantilla_ubicacion_nombre",
                columnNames = {"ubicacion_id", "nombre"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TarjetaPlantilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoTarjeta tipo;

    @Column(nullable = false, length = 100)
    private String nombre;

    // Precio del producto asociado: cada estampilla o visita cuenta como una compra de este monto
    @Column(precision = 10, scale = 2)
    private BigDecimal precioProducto;

    // --- Diseño visual ---
    @Column(nullable = false, length = 7)
    private String colorFondo;

    @Column(nullable = false, length = 7)
    private String colorTexto;

    private String logoUrl;

    private String imagenFondoUrl;

    // Nombre de un ícono o URL de una imagen para el sello conseguido y el pendiente
    private String iconoSelloActivo;

    private String iconoSelloInactivo;

    // --- Reverso de la tarjeta ---
    @Column(length = 500)
    private String descripcion;

    @Column(length = 20)
    private String telefono;

    @Column(length = 150)
    private String sitioWeb;

    // --- Solo ESTAMPILLAS ---
    private Integer metaSellos;

    @Column(length = 150)
    private String recompensa;

    // Tarjeta a la que pasa el cliente tras canjear. Si está vacía, repite esta misma.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "siguiente_plantilla_id")
    private TarjetaPlantilla siguientePlantilla;

    // --- Solo NIVELES ---
    @ElementCollection
    @CollectionTable(name = "tarjetas_plantilla_niveles", joinColumns = @JoinColumn(name = "plantilla_id"))
    @OrderBy("visitasMinimas ASC")
    @Builder.Default
    private List<NivelConfig> niveles = new ArrayList<>();

    // --- Solo GIFTCARD ---
    @Column(precision = 10, scale = 2)
    private BigDecimal saldoInicial;

    @Builder.Default
    @Column(nullable = false)
    private boolean activa = true;

    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    // Cada plantilla pertenece siempre a una sede
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }
}