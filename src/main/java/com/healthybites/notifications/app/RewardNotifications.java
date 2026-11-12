package com.healthybites.notifications.app;

import com.healthybites.rewards.RewardDirectory;
import com.healthybites.rewards.RewardDirectory.PrizeKind;
import com.healthybites.rewards.RewardDirectory.RewardSummary;
import com.healthybites.rewards.events.RewardEarned;
import com.healthybites.shared.mail.EmailContent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Enhorabuena al cliente que completa una racha: su premio ya está desbloqueado. */
@Component
class RewardNotifications {

    private final Notifier notifier;
    private final RewardDirectory rewards;
    private final AppLinks links;

    RewardNotifications(Notifier notifier, RewardDirectory rewards, AppLinks links) {
        this.notifier = notifier;
        this.rewards = rewards;
        this.links = links;
    }

    @ApplicationModuleListener
    void on(RewardEarned event) {
        rewards.find(event.rewardId())
                .ifPresent(reward -> notifier.notify(event.clientId(), account -> earned(account.firstName(), reward)));
    }

    private EmailContent earned(String firstName, RewardSummary reward) {
        String prize = (reward.prizeKind() == PrizeKind.MEAL_PLAN ? "el plan alimenticio" : "el contenido") + " «"
                + reward.prizeTitle() + "»";
        return EmailContent.builder("¡Lo has conseguido! Ya tienes tu premio")
                .title("¡Racha completada!")
                .greeting(firstName)
                .paragraph("Has registrado tus hábitos " + reward.streakDays() + " días seguidos y has completado «"
                        + reward.title() + "».")
                .paragraph("Tu premio ya está desbloqueado: " + prize + ". Sigue así: cada día cuenta.")
                .action("Ver mis recompensas", links.rewards())
                .build();
    }
}
