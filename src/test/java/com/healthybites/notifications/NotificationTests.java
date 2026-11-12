package com.healthybites.notifications;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockingDetails;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.billing.Tier;
import com.healthybites.billing.events.SubscriptionActivated;
import com.healthybites.billing.events.SubscriptionExpired;
import com.healthybites.community.events.MembershipRequested;
import com.healthybites.rewards.events.RewardEarned;
import com.healthybites.support.Seed;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Avisos por correo que salen de los eventos de los demás módulos. Los eventos se publican dentro de una transacción,
 * como en la aplicación; el SMTP está simulado y cada correo se captura entero (HTML y texto plano).
 */
class NotificationTests extends IntegrationTest {

    private static final UUID MARIA_SUBSCRIPTION = UUID.fromString("00000000-0000-7000-8000-000000000b01");
    private static final UUID MARIA_PAYMENT = UUID.fromString("00000000-0000-7000-8000-000000000c01");
    private static final UUID SEVEN_DAY_REWARD = UUID.fromString("00000000-0000-7000-8000-000000002001");
    private static final UUID PEDRO_PRIVATE_GROUP = UUID.fromString("00000000-0000-7000-8000-000000003003");

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private TransactionTemplate transactions;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private FailedEventPublications failedEvents;

    @Test
    void aNewClientIsWelcomedWithALinkToTheirPanel() throws Exception {
        NewUser client = newClient("bienvenida");

        Email email = singleEmailTo(client.email());
        assertThat(email.subject()).isEqualTo("Te damos la bienvenida a Healthy Bites");
        assertThat(email.html()).contains("Hola, Prueba:").contains("href=\"http://localhost:4210/client\"");
        assertThat(email.text()).contains("Ir a mi panel: http://localhost:4210/client");
    }

    @Test
    void anActivatedSubscriptionSendsTheReceipt() throws Exception {
        publish(new SubscriptionActivated(MARIA_SUBSCRIPTION, Seed.MARIA_ID, Tier.PREMIUM, MARIA_PAYMENT));

        Email email = singleEmailTo(Seed.MARIA);
        assertThat(email.subject()).isEqualTo("Tu suscripción Premium ya está activa");
        assertThat(email.text())
                .contains("Importe: 5,99 USD")
                .contains("Referencia de PayPal: SEED-CAPTURE-MARIA")
                .contains("Ver mi suscripción: http://localhost:4210/client/subscription");
    }

    @Test
    void anEndedSubscriptionSaysWhatAccessRemains() throws Exception {
        NewUser client = newClient("caducada");

        publish(new SubscriptionExpired(UUID.randomUUID(), client.id(), Tier.VIP));

        assertThat(emailsTo(client.email()))
                .filteredOn(email -> email.subject().equals("Tu suscripción VIP ha terminado"))
                .singleElement()
                .satisfies(email -> assertThat(email.text())
                        .contains("Conservas tu cuenta, tu progreso y todo el contenido básico."));
    }

    @Test
    void completingAStreakAnnouncesThePrize() throws Exception {
        publish(new RewardEarned(SEVEN_DAY_REWARD, Seed.JUAN_ID));

        String reward = jdbc.queryForObject("select title from rewards where id = ?", String.class, SEVEN_DAY_REWARD);
        Email email = singleEmailTo(Seed.JUAN);
        assertThat(email.subject()).isEqualTo("¡Lo has conseguido! Ya tienes tu premio");
        assertThat(email.text())
                .contains("has completado «" + reward + "»")
                .contains("Ver mis recompensas: http://localhost:4210/client/rewards");
    }

    @Test
    void theOwnerOfAPrivateGroupHearsAboutRequestsToJoin() throws Exception {
        publish(new MembershipRequested(PEDRO_PRIVATE_GROUP, Seed.JUAN_ID, Seed.PEDRO_ID));

        String group = jdbc.queryForObject(
                "select name from community_groups where id = ?", String.class, PEDRO_PRIVATE_GROUP);
        String juan = jdbc.queryForObject(
                "select first_name || ' ' || last_name from users where id = ?", String.class, Seed.JUAN_ID);
        Email email = singleEmailTo(Seed.PEDRO);
        assertThat(email.subject()).isEqualTo(juan + " quiere unirse a «" + group + "»");
        assertThat(email.html())
                .contains("href=\"http://localhost:4210/client/community/groups/" + PEDRO_PRIVATE_GROUP);
    }

    @Test
    void deactivatedAccountsReceiveNothing() throws Exception {
        NewUser client = newClient("desactivada");
        mockMvc.perform(post("/admin/users/" + client.id() + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isOk());

        publish(new RewardEarned(SEVEN_DAY_REWARD, client.id()));

        assertThat(emailsTo(client.email()))
                .extracting(Email::subject)
                .containsExactly("Te damos la bienvenida a Healthy Bites");
    }

    @Test
    void anEmailThatFailsIsKeptAndDeliveredOnRetry() throws Exception {
        NewUser client = newClient("reintento");
        doThrow(new MailSendException("SMTP caído")).when(mailSender).send(any(MimeMessage.class));

        publish(new SubscriptionExpired(UUID.randomUUID(), client.id(), Tier.PREMIUM));
        assertThat(pendingEventsAbout(client.id())).isOne();

        doNothing().when(mailSender).send(any(MimeMessage.class));
        failedEvents.resubmit(ResubmissionOptions.defaults()
                .withMinAge(Duration.ZERO)
                .withFilter(publication -> publication.getEvent() instanceof SubscriptionExpired event
                        && event.userId().equals(client.id())));

        assertThat(pendingEventsAbout(client.id())).isZero();
        // El intento fallido y el bueno.
        assertThat(emailsTo(client.email()))
                .extracting(Email::subject)
                .containsOnlyOnce("Te damos la bienvenida a Healthy Bites")
                .filteredOn(subject -> subject.equals("Tu suscripción Premium ha terminado"))
                .hasSize(2);
    }

    // ---------------------------------------------------------------- utilidades

    private record Email(String subject, String html, String text) {}

    private void publish(Object event) {
        transactions.executeWithoutResult(status -> publisher.publishEvent(event));
    }

    private int pendingEventsAbout(UUID userId) {
        Integer pending = jdbc.queryForObject(
                "select count(*) from event_publication where completion_date is null and serialized_event like ?",
                Integer.class,
                "%" + userId + "%");
        return pending == null ? 0 : pending;
    }

    private Email singleEmailTo(String address) throws Exception {
        List<Email> emails = emailsTo(address);
        assertThat(emails).hasSize(1);
        return emails.getFirst();
    }

    /** Los correos que este test ha intentado enviar a una dirección, en orden. */
    private List<Email> emailsTo(String address) throws Exception {
        List<Email> emails = new ArrayList<>();
        for (var invocation : mockingDetails(mailSender).getInvocations()) {
            if (invocation.getMethod().getName().equals("send")
                    && invocation.getArguments().length == 1
                    && invocation.getArgument(0) instanceof MimeMessage message
                    && message.getRecipients(Message.RecipientType.TO)[0]
                            .toString()
                            .equals(address)) {
                List<String> html = new ArrayList<>();
                List<String> text = new ArrayList<>();
                collectBodies(message, html, text);
                emails.add(new Email(message.getSubject(), String.join("", html), String.join("", text)));
            }
        }
        return emails;
    }

    private static void collectBodies(Part part, List<String> html, List<String> text) throws Exception {
        Object content = part.getContent();
        if (content instanceof Multipart multipart) {
            for (int i = 0; i < multipart.getCount(); i++) {
                collectBodies(multipart.getBodyPart(i), html, text);
            }
        } else if (content instanceof String body) {
            (part.getDataHandler().getContentType().startsWith("text/html") ? html : text).add(body);
        }
    }
}
