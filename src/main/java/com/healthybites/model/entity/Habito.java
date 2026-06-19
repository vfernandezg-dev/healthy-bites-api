package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "habito")
public class Habito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    @Column(name = "hidratacion", nullable = false)
    private float hidratacion;

    @Column(name = "alimentacion", nullable = false)
    private float alimentacion;

    @Column(name = "ejercicio", nullable = false)
    private float ejercicio;

    @Column(name = "calidad_de_sueno", nullable = false)
    private float calidadDeSueno;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_habito_cliente"))
    private Cliente cliente;
}
