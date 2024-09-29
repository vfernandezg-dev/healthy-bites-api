package com.healthybites.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Data
@Entity
@Table(name = "grupo")
public class Grupo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "es_privado", nullable = false)
    private boolean esPrivado;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL)
    private List<Cliente> clientes;

    @JsonIgnore
    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL)
    private List<Publicacion> publicaciones;
}
