package com.healthybites.model.entity;

import com.healthybites.model.enums.EstadoMeta;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "meta")
public class Meta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "peso_objetivo", nullable = false)
    private double pesoObjetivo;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_actualizacion", nullable = false)
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "meta", cascade = CascadeType.ALL)
    private List<Seguimiento> seguimiento;

    @ManyToOne
    @JoinColumn(name = "id_cliente", referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "FK_meta_cliente"))
    private Cliente cliente;

    // Estado actual de la meta. Nullable para no romper filas antiguas con ddl-auto=update.
    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoMeta estado = EstadoMeta.ACTIVE;

    // Historial cronologico de estados por los que ha pasado la meta.
    @ElementCollection
    @CollectionTable(name = "meta_historial_estado",
            joinColumns = @JoinColumn(name = "id_meta"),
            foreignKey = @ForeignKey(name = "FK_metahistorial_meta"))
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    @OrderColumn(name = "orden")
    private List<EstadoMeta> historialEstados = new ArrayList<>();
}
