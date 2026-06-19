package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "acceso_plan")
@IdClass(AccesoPlan.class)
public class AccesoPlan {
    @Id
    private Integer cliente;

    @Id
    private Integer planAlimenticio;
}
