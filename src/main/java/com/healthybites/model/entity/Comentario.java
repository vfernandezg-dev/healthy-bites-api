package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "comentario")
public class Comentario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "mensaje", nullable = false)
    private String mensaje;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_comentario_cliente"))
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "id_publicacion", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_comentario_publicacion"))
    private Publicacion publicacion;

}
