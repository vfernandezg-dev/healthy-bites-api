package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "racha")
@IdClass(RachaPK.class)
public class Racha {
    @Id
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Cliente cliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "recompensa_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Recompensa recompensa;

    @Column(name = "contador_dias", nullable = false)
    private int contadorDias;

    @Column(name = "entregada", nullable = false)
    private boolean entregada;

    @Column(name = "ultima_fecha_registro", nullable = false)
    private LocalDateTime ultimaFechaRegistro;

}
