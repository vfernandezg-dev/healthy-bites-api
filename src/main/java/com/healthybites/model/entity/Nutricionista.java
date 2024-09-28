package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "nutricionista")
public class Nutricionista {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @OneToMany(mappedBy = "nutricionista", cascade = CascadeType.ALL)
    private List<PlanAlimenticio> planesAlimenticios;

    @OneToMany(mappedBy = "nutricionista", cascade = CascadeType.ALL)
    private List<Contenido> contenidos;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_nutricionista_usuario"))
    private Usuario usuario;
}
