package com.healthybites.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healthybites.model.enums.TipoSuscripcion;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Data
@Entity
@Table(name = "suscripcion")
public class Suscripcion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_suscripcion", nullable = false)
    private TipoSuscripcion tipoSuscripcion;

    @Column(name = "precio", nullable = false)
    private double precio;

    @Column(name = "fecha_inicio", nullable = false)
    private Date fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private Date fechaFin;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_suscripcion_cliente"))
    private Cliente cliente;
}
