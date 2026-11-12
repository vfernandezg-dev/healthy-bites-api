/**
 * Suscripciones y pagos: planes con su precio en la base de datos, checkout con PayPal (URLs de vuelta del servidor,
 * captura verificada e idempotente) y el nivel de acceso de cada usuario.
 *
 * <p>API para otros módulos: {@link com.healthybites.billing.Entitlements} y {@link com.healthybites.billing.Tier};
 * eventos en {@code events}.
 */
@ApplicationModule(displayName = "Billing")
@NullMarked
package com.healthybites.billing;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
