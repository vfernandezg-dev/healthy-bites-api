package com.healthybites.profiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** Datos físicos de los clientes para otros módulos (recomendación de peso, clientes de un nutricionista). */
public interface ClientProfiles {

    Optional<ClientProfileView> find(UUID userId);

    Map<UUID, ClientProfileView> findAll(Collection<UUID> userIds);

    record ClientProfileView(
            UUID userId,
            @Nullable Sex sex,
            @Nullable LocalDate birthDate,
            @Nullable BigDecimal heightCm,
            @Nullable BigDecimal weightKg) {}
}
