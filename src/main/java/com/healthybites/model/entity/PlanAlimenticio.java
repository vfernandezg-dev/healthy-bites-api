package com.healthybites.model.entity;

import com.healthybites.model.enums.PlanObjetivo;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "plan_alimenticio")
public class PlanAlimenticio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_objetivo", nullable = false)
    private PlanObjetivo planObjetivo;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "duracion_dias", nullable = false)
    private int duracionDias;

    @Column(name = "es_gratis", nullable = false)
    private boolean esGratis;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "planAlimenticio", cascade = CascadeType.ALL)
    private List<ComidaDiaria> comidasDiarias;

    @ManyToOne
    @JoinColumn(name = "id_nutricionista", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_planalimenticio_nutricionista"))
    private Nutricionista nutricionista;

}
