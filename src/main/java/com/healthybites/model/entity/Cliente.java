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

    @OneToOne
    @JoinColumn(name = "id_usuario", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_cliente_usuario"))
    private Usuario usuario;

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Recompensa> recompensas;

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL)
    private List<Habito> habitos;

    public void setCreatedAt(LocalDateTime now) {
    }

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Meta> metas;

}
