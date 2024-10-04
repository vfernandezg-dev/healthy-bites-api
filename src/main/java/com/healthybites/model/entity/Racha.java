package com.healthybites.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "racha")
public class Racha {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dias_consecutivos", nullable = false)
    private int diasConsecutivos;

    @Column(name = "ultima_fecha_registro", nullable = false)
    private Date ultimaFechaRegistro;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_racha_cliente"))
    private Cliente cliente;
}
