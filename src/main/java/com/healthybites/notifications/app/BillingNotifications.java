package com.healthybites.notifications.app;

import com.healthybites.billing.Entitlements;
import com.healthybites.billing.Receipts;
import com.healthybites.billing.Receipts.Receipt;
import com.healthybites.billing.Tier;
import com.healthybites.billing.events.SubscriptionActivated;
import com.healthybites.billing.events.SubscriptionExpired;
import com.healthybites.shared.config.AppProperties;
import com.healthybites.shared.mail.EmailContent;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Justificante cuando se activa una suscripción y aviso cuando termina su período pagado. */
@Slf4j
@Component
class BillingNotifications {

    private final Notifier notifier;
    private final Receipts receipts;
    private final Entitlements entitlements;
    private final AppLinks links;
    private final AppProperties app;

    BillingNotifications(
            Notifier notifier, Receipts receipts, Entitlements entitlements, AppLinks links, AppProperties app) {
        this.notifier = notifier;
        this.receipts = receipts;
        this.entitlements = entitlements;
        this.links = links;
        this.app = app;
    }

    @ApplicationModuleListener
    void on(SubscriptionActivated event) {
        receipts.find(event.paymentId())
                .ifPresentOrElse(
                        receipt -> notifier.notify(event.userId(), account -> activated(account.firstName(), receipt)),
                        () -> log.warn(
                                "Sin justificante para el pago {}: no se envía la confirmación", event.paymentId()));
    }

    @ApplicationModuleListener
    void on(SubscriptionExpired event) {
        Tier current = entitlements.tierOf(event.userId());
        notifier.notify(event.userId(), account -> expired(account.firstName(), event.tier(), current));
    }

    private EmailContent activated(String firstName, Receipt receipt) {
        String tier = Formats.tier(receipt.tier());
        EmailContent.Builder email = EmailContent.builder("Tu suscripción " + tier + " ya está activa")
                .greeting(firstName)
                .paragraph("Hemos recibido tu pago. Desde ahora tienes acceso a todos los planes y contenidos de tu"
                        + " nivel " + tier + ".")
                .detail("Suscripción", tier)
                .detail("Importe", Formats.money(receipt.amount()))
                .detail("Fecha del pago", Formats.date(receipt.paidAt(), app.timeZone()));
        Instant periodEnd = receipt.periodEnd();
        if (periodEnd != null) {
            email.detail("Acceso hasta", Formats.date(periodEnd, app.timeZone()));
        }
        return email.detail("Referencia de PayPal", receipt.reference())
                .action("Ver mi suscripción", links.subscription())
                .footnote("Guarda este correo como justificante. Puedes cancelar la suscripción cuando quieras y"
                        + " mantendrás el acceso hasta el final del período pagado.")
                .build();
    }

    private EmailContent expired(String firstName, Tier ended, Tier current) {
        String tier = Formats.tier(ended);
        String remaining = current == Tier.BASIC
                ? "Conservas tu cuenta, tu progreso y todo el contenido básico."
                : "Conservas el acceso de tu suscripción " + Formats.tier(current) + ".";
        return EmailContent.builder("Tu suscripción " + tier + " ha terminado")
                .greeting(firstName)
                .paragraph("El período pagado de tu suscripción " + tier + " ha terminado. " + remaining)
                .paragraph("Cuando quieras, puedes volver a suscribirte para recuperar los planes y contenidos " + tier
                        + ".")
                .action("Ver las suscripciones", links.subscription())
                .build();
    }
}
