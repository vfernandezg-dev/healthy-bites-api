package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "racha")
@IdClass(RachaPK.class)
public class Racha {
    // Mismo patrón que Unirse. Con insertable/updatable = false en las columnas del id, Hibernate 7 no sabe
    // construir el UPDATE ("Unable to locate parameter racha.cliente_id") y registrar un hábito daba 500.
    @Id
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_racha_cliente"))
    private Cliente cliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "recompensa_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_racha_recompensa"))
    private Recompensa recompensa;

    @Column(name = "contador_dias", nullable = false)
    private int contadorDias;

    @Column(name = "entregada", nullable = false)
    private boolean entregada;

    @Column(name = "ultima_fecha_registro", nullable = false)
    private LocalDateTime ultimaFechaRegistro;

}
