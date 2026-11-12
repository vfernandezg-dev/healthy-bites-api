package com.healthybites.shared.domain;

import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;
import org.jspecify.annotations.Nullable;

/**
 * Base de los agregados con id propio: UUIDv7 (ordenable por tiempo, sin secuencias ni ids adivinables), además del
 * bloqueo optimista y las fechas de {@link AuditedEntity}.
 *
 * <p>La igualdad se basa en el id, así que dos instancias sin guardar nunca son iguales; el {@code hashCode} es el de
 * la clase para que no cambie al asignarse el id.
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity extends AuditedEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private @Nullable UUID id;

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

    /** Id ya asignado (la entidad está guardada). */
    public UUID requireId() {
        if (id == null) {
            throw new IllegalStateException("La entidad aún no tiene id");
        }
        return id;
    }
}
