package com.healthybites.shared.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Importe con moneda. Siempre con dos decimales; en JSON el importe va como texto ({@code "5.99"}) para que ningún
 * cliente lo convierta en un número de coma flotante.
 */
@Embeddable
public record Money(
        @JsonFormat(shape = JsonFormat.Shape.STRING) @Column(name = "amount", precision = 10, scale = 2)
        BigDecimal amount,

        @Column(name = "currency", length = 3) String currency) {

    public Money {
        amount = amount.setScale(2, RoundingMode.HALF_UP);
        currency = currency.toUpperCase(Locale.ROOT);
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    /** {@code 5.99}, como lo esperan los proveedores de pago. */
    public String plain() {
        return amount.toPlainString();
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }
}
