// PlanAlimenticio.java
package com.healthybites.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.healthybites.model.enums.PlanObjetivo;
import jakarta.persistence.*;
import lombok.Data;

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

    @JsonIgnore
    @OneToMany(mappedBy = "planAlimenticio", cascade = CascadeType.ALL)
    private List<ComidaDiaria> comidasDiarias;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "nutricionista_id", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_planalimenticio_nutricionista"))
    private Nutricionista nutricionista;
}