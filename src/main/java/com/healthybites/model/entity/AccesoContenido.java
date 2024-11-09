package com.healthybites.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "acceso_contenido")
@IdClass(AccesoContenidoPK.class)
public class AccesoContenido {
    @Id
    @ManyToOne
    @JoinColumn(name = "cliente_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_accesocontenido_cliente"))
    private Cliente cliente;

    @Id
    @ManyToOne
    @JoinColumn(name = "contenido_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_accesocontenido_contenido"))
    private Contenido contenido;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
}
