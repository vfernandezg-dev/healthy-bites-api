package com.healthybites.billing;

import java.util.UUID;

/** Nivel de acceso de un usuario, para los módulos que tienen contenido de pago (content, nutrition). */
public interface Entitlements {

    /**
     * El nivel más alto de sus suscripciones vigentes: activas, o canceladas cuyo período pagado aún no terminó.
     * {@link Tier#BASIC} si no tiene ninguna.
     */
    Tier tierOf(UUID userId);
}
