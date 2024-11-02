package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "cliente")
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "sexo", nullable = false)
    private String sexo;

    @Column(name = "edad", nullable = false)
    private int edad;

    @Column(name = "altura", nullable = false)
    private double altura;

    @Column(name = "peso", nullable = false)
    private double peso;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToOne
    @JoinColumn(name = "id_racha", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_cliente_racha"))
    private Racha racha;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Habito> habitos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<AccesoContenido> accesoContenidos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<AccesoPlan> planesAlimenticios;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Grupo> grupos;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Unirse> unirse;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Meta> meta;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Publicacion> publicaciones;

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id")
    private Usuario usuario;

}
