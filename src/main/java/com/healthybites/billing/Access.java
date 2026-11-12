package com.healthybites.billing;

import com.healthybites.shared.web.ApiException;
import java.util.Set;
import java.util.UUID;

/**
 * Qué puede abrir un lector del contenido de pago (contenidos y planes): todo si es del staff; si no, lo de su nivel
 * o inferior y lo que desbloqueó con recompensas. Se calcula una vez y decide sobre un listado entero sin más
 * consultas.
 */
public record Access(boolean staff, Tier tier, Set<UUID> unlocked) {

    public static Access forStaff() {
        return new Access(true, Tier.VIP, Set.of());
    }

    public boolean canOpen(Tier required, UUID itemId) {
        return staff || tier.includes(required) || unlocked.contains(itemId);
    }

    /** 403 {@code subscription-required} con el nivel necesario ({@code requiredTier}), igual en todos los módulos. */
    public static ApiException subscriptionRequired(Tier required, String detail) {
        return ApiException.forbidden("subscription-required", detail).with("requiredTier", required);
    }
}
