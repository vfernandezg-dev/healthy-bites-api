package com.healthybites.notifications.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.healthybites.billing.Tier;
import com.healthybites.shared.domain.Money;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class FormatsTests {

    @Test
    void amountsUseSpanishSeparatorsAndTheCurrencyCode() {
        assertThat(Formats.money(Money.of("5.99", "usd"))).isEqualTo("5,99 USD");
        assertThat(Formats.money(Money.of("1234.5", "USD"))).isEqualTo("1.234,50 USD");
    }

    @Test
    void datesAreWrittenOutInTheApplicationTimeZone() {
        // Las 03:00 UTC del día 12 todavía son el día 11 en Lima (UTC-5).
        Instant instant = Instant.parse("2026-11-12T03:00:00Z");

        assertThat(Formats.date(instant, ZoneId.of("America/Lima"))).isEqualTo("11 de noviembre de 2026");
        assertThat(Formats.date(instant, ZoneId.of("UTC"))).isEqualTo("12 de noviembre de 2026");
    }

    @Test
    void tiersHaveTheirSpanishNames() {
        assertThat(Formats.tier(Tier.BASIC)).isEqualTo("Básico");
        assertThat(Formats.tier(Tier.VIP)).isEqualTo("VIP");
    }
}
