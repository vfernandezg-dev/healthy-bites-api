package com.healthybites.model.entity;

import com.healthybites.model.enums.CategoriaComida;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comida_diaria")
public class ComidaDiaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre_comida", nullable = false)
    private String nombreComida;

    @Column(name = "calorias", nullable = false)
    private int calorias;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false)
    private CategoriaComida categoria;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "id_planalimenticio", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_comidadiaria_planalimenticio"))
    private PlanAlimenticio planAlimenticio;
}
