package com.healthybites.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Column(nullable = false, unique = true)
    private String correo;

    private String contrasena;

    @Column(name = "sexo", nullable = false)
    private String sexo;

    @Column(name = "edad", nullable = false)
    private int edad;

    @Column(name = "altura", nullable = false)
    private double altura;

    @Column(name = "peso", nullable = false)
    private double peso;

    @ManyToOne
    @JoinColumn(name = "id_grupo", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_cliente_grupo"))
    private Grupo grupo;

    @ManyToOne
    @JoinColumn(name = "id_meta", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_cliente_meta"))
    private Meta meta;

    @ManyToOne
    @JoinColumn(name = "id_suscripcion", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_cliente_suscripcion"))
    private Suscripcion suscripcion;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Racha> rachas;

}
