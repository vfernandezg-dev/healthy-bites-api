package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "unirse")
@IdClass(UnirsePK.class)
public class Unirse {
    // Mismo patrón que AccesoContenido: Hibernate 7 exige que el @Id derivado sea la entidad, no su id.
    @Id
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_unirse_cliente"))
    private Cliente cliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "grupo_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_unirse_grupo"))
    private Grupo grupo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
