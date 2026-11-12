package com.healthybites.shared.web;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.jspecify.annotations.Nullable;

/**
 * Rangos de fechas de los listados: la inicial no va después de la final y, como mucho, {@value #MAX_DAYS} días. Así
 * una petición no puede pedir años de datos de una vez.
 */
public final class DateRanges {

    public static final int MAX_DAYS = 366;

    private DateRanges() {}

    /** @throws ApiException 422 {@code invalid-range} o {@code range-too-large} */
    public static void check(@Nullable LocalDate from, @Nullable LocalDate to) {
        if (from == null || to == null) {
            return;
        }
        if (from.isAfter(to)) {
            throw ApiException.unprocessable("invalid-range", "La fecha inicial es posterior a la final");
        }
        if (ChronoUnit.DAYS.between(from, to) >= MAX_DAYS) {
            throw ApiException.unprocessable("range-too-large", "El rango no puede superar " + MAX_DAYS + " días")
                    .with("maxDays", MAX_DAYS);
        }
    }
}
