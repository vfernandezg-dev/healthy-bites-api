package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "unirse")
@IdClass(UnirsePK.class)
public class Unirse {
    @Id
    private Integer cliente;

    @Id
    private Integer grupo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
