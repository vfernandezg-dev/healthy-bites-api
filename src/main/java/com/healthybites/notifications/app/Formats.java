package com.healthybites.notifications.app;

import com.healthybites.billing.Tier;
import com.healthybites.shared.domain.Money;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Cómo se escriben importes, fechas y niveles en los correos (español). */
final class Formats {

    static final Locale SPANISH = Locale.forLanguageTag("es");

    private static final DateTimeFormatter LONG_DATE = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", SPANISH);

    private Formats() {}

    /** {@code 5,99 USD}. */
    static String money(Money money) {
        return String.format(SPANISH, "%,.2f %s", money.amount(), money.currency());
    }

    /** {@code 12 de noviembre de 2026}, en la zona horaria de la aplicación. */
    static String date(Instant instant, ZoneId zone) {
        return LONG_DATE.format(instant.atZone(zone));
    }

    static String tier(Tier tier) {
        return switch (tier) {
            case BASIC -> "Básico";
            case PREMIUM -> "Premium";
            case VIP -> "VIP";
        };
    }
}
