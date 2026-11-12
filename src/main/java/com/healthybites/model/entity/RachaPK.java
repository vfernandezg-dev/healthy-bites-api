package com.healthybites.model.entity;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Clave de {@link Racha} como identidad derivada estándar de JPA: cada atributo se llama como el {@code @Id} de la
 * entidad y tiene el tipo de la clave del padre. Con las entidades dentro (lo admite Hibernate, no la especificación)
 * Hibernate 7 no sabía construir el UPDATE de una racha y registrar un hábito devolvía 500.
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RachaPK implements Serializable {

    private Integer cliente;

    private Integer recompensa;
}
