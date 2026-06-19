package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
public class AccesoPlanPK implements Serializable {

    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_accesoplan_cliente"))
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "plan_alimenticio_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_accesoplan_alimenticio"))
    private PlanAlimenticio planAlimenticio;
}
