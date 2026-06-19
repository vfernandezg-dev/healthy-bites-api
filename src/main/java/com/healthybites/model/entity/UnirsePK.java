package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
public class UnirsePK implements Serializable {
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_unirse_cliente"))
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "grupo_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_unirse_grupo"))
    private Grupo grupo;
}
