package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "acceso_plan")
@IdClass(AccesoPlanPK.class)
public class AccesoPlan {
    // Mismo patrón que AccesoContenido: Hibernate 7 exige que el @Id derivado sea la entidad, no su id.
    @Id
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_accesoplan_cliente"))
    private Cliente cliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "plan_alimenticio_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_accesoplan_alimenticio"))
    private PlanAlimenticio planAlimenticio;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
