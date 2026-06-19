package com.healthybites.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "habito")
public class Habito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "hidratacion", nullable = false)
    private float hidratacion;

    @Column(name = "alimentacion", nullable = false)
    private float alimentacion;

    @Column(name = "ejercicio", nullable = false)
    private float ejercicio;

    @Column(name = "calidad_de_sueno", nullable = false)
    private float calidadDeSueno;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_habito_cliente"))
    private Cliente cliente;
}
