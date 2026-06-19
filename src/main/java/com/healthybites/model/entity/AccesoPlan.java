package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "acceso_plan")
@IdClass(AccesoPlanPK.class)
public class AccesoPlan {
    @Id
    private Integer cliente;

    @Id
    private Integer planAlimenticio;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
