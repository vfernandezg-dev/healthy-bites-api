package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
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

    @Column(name = "bio")
    private String bio;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "nutricionista", cascade = CascadeType.ALL)
    private List<PlanAlimenticio> planesAlimenticios;

    @OneToMany(mappedBy = "nutricionista", cascade = CascadeType.ALL)
    private List<Contenido> contenidos;

    @OneToMany(mappedBy = "nutricionista", cascade = CascadeType.ALL)
    private List<Recompensa> recompensas;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario usuario;
}
