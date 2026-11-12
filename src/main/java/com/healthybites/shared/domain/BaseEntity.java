package com.healthybites.shared.domain;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * Base de los agregados que cambian: id UUIDv7 (ordenable por tiempo, sin secuencias ni ids adivinables),
 * bloqueo optimista con {@code version} y fechas de creación y modificación (con el {@code Clock} de la aplicación).
 *
 * <p>La igualdad se basa en el id, así que dos instancias sin guardar nunca son iguales; el {@code hashCode} es el de
 * la clase para que no cambie al asignarse el id.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class BaseEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

    @Version
    private long version;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private @Nullable Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private @Nullable Instant updatedAt;

    @Override
    public final boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) {
            return false;
        }
        return id != null && id.equals(((BaseEntity) other).getId());
    }

    @Override
    public final int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
