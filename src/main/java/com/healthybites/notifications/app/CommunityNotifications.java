package com.healthybites.notifications.app;

import com.healthybites.community.GroupDirectory;
import com.healthybites.community.events.MembershipRequested;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.shared.mail.EmailContent;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

/** Aviso al creador de un grupo privado cuando alguien pide entrar. */
@Component
class CommunityNotifications {

    private final Notifier notifier;
    private final GroupDirectory groups;
    private final UserAccounts accounts;
    private final AppLinks links;

    CommunityNotifications(Notifier notifier, GroupDirectory groups, UserAccounts accounts, AppLinks links) {
        this.notifier = notifier;
        this.groups = groups;
        this.accounts = accounts;
        this.links = links;
    }

    @ApplicationModuleListener
    void on(MembershipRequested event) {
        String requester = accounts.findById(event.requesterId())
                .map(UserAccount::fullName)
                .orElse("Una persona");
        groups.nameOf(event.groupId())
                .ifPresent(group -> notifier.notify(event.ownerId(), owner -> request(owner, requester, group, event)));
    }

    private EmailContent request(UserAccount owner, String requester, String group, MembershipRequested event) {
        return EmailContent.builder(requester + " quiere unirse a «" + group + "»")
                .title("Nueva solicitud para tu grupo")
                .greeting(owner.firstName())
                .paragraph(requester + " ha pedido unirse a «" + group + "», tu grupo privado.")
                .paragraph("Revisa la solicitud para aceptarla o rechazarla. Hasta entonces no podrá leer ni publicar"
                        + " en el grupo.")
                .action("Revisar la solicitud", links.group(owner.role(), event.groupId()))
                .build();
    }
}
