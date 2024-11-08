package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
public class RachaPK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_racha_cliente"))
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "recompensa_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_racha_recompensa"))
    private Recompensa recompensa;
}
