package com.healthybites.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.Instant;
import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Bloqueo optimista ({@code version}) y fechas de creación y modificación (con el {@code Clock} de la aplicación).
 * La usan directamente las entidades cuyo id no se genera (los perfiles usan el id del usuario); el resto extiende
 * {@link BaseEntity}.
 *
 * <p>La versión es un {@code Long}: mientras es {@code null}, Spring Data trata la entidad como nueva aunque su id
 * ya esté asignado.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class AuditedEntity {

    @Version
    private @Nullable Long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private @Nullable Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private @Nullable Instant updatedAt;
}
