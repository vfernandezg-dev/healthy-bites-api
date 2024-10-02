package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "unirse")
@IdClass(Unirse.class)
public class Unirse {
    @Id
    private Integer cliente;

    @Id
    private Integer grupo;
}
