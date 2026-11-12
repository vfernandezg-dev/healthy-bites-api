package com.healthybites.notifications.app;

import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.shared.mail.EmailContent;
import com.healthybites.shared.mail.MailService;
import java.util.UUID;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Envía un aviso a una cuenta. Las cuentas desactivadas o que ya no existen no reciben nada. Un fallo del SMTP se
 * propaga a propósito: así el evento queda como fallido y se reintenta.
 */
@Slf4j
@Component
class Notifier {

    private final UserAccounts accounts;
    private final MailService mail;

    Notifier(UserAccounts accounts, MailService mail) {
        this.accounts = accounts;
        this.mail = mail;
    }

    void notify(UUID userId, Function<UserAccount, EmailContent> content) {
        accounts.findById(userId)
                .filter(UserAccount::active)
                .ifPresentOrElse(
                        account -> mail.send(account.email(), content.apply(account)),
                        () -> log.debug("Aviso omitido: la cuenta {} no existe o está desactivada", userId));
    }
}
