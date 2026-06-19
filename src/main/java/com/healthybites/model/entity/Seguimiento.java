package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "seguimiento")
public class Seguimiento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "peso_del_dia", nullable = false)
    private double pesoDelDia;

    @Column(name = "obsevaciones", nullable = false)
    private String observaciones;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "id_meta", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_seguimiento_meta"))
    private Meta meta;
}
