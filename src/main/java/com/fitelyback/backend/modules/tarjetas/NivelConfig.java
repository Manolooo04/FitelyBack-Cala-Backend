package com.fitelyback.backend.modules.tarjetas;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// Un nivel dentro de una tarjeta de tipo NIVELES (por ejemplo: Plata, desde 10 visitas)
@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NivelConfig {

    @Column(nullable = false, length = 50)
    private String nombre;

    @Column(nullable = false)
    private Integer visitasMinimas;

    @Column(length = 150)
    private String beneficio;
}