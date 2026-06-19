package com.healthybites.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "contenido")
public class Contenido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contenido", nullable = false)
    private TipoContenido tipoContenido;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_contenido", nullable = false)
    private CategoriaContenido categoriaContenido;

    @Column(name = "es_gratis", nullable = false)
    private boolean esGratis;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne
    @JoinColumn(name = "id_nutricionista", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_contenido_nutricionista"))
    private Nutricionista nutricionista;
}
