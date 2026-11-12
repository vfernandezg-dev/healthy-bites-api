package com.healthybites.notifications.app;

import com.healthybites.identity.Role;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.events.UserRegistered;
import com.healthybites.shared.mail.EmailContent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Bienvenida a cada cuenta nueva: clientes que se registran y nutricionistas que aceptan su invitación. */
@Component
class AccountNotifications {

    private final Notifier notifier;
    private final AppLinks links;

    AccountNotifications(Notifier notifier, AppLinks links) {
        this.notifier = notifier;
        this.links = links;
    }

    @ApplicationModuleListener
    void on(UserRegistered event) {
        notifier.notify(event.userId(), this::welcome);
    }

    private EmailContent welcome(UserAccount account) {
        EmailContent.Builder email =
                EmailContent.builder("Te damos la bienvenida a Healthy Bites").greeting(account.firstName());
        if (account.role() == Role.NUTRITIONIST) {
            email.paragraph("Tu cuenta de nutricionista ya está activa. Crea tu primer plan alimenticio, publica"
                            + " contenido y prepara recompensas que motiven a tus clientes.")
                    .paragraph("Completa tu perfil con una breve presentación: es lo primero que verán tus clientes.");
        } else {
            email.paragraph("Tu cuenta ya está lista. Para empezar, marca tu primera meta, registra tus hábitos"
                            + " cada día y descubre los planes y contenidos de nuestros nutricionistas.")
                    .paragraph("Con Premium o VIP desbloqueas además los planes y contenidos exclusivos.");
        }
        return email.action("Ir a mi panel", links.home(account.role()))
                .footnote("Si no has creado tú esta cuenta, escríbenos respondiendo a este correo.")
                .build();
    }
}
