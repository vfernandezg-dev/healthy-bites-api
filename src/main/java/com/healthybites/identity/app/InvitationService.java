package com.healthybites.identity.app;

import com.healthybites.identity.domain.NutritionistInvitation;
import com.healthybites.identity.domain.NutritionistInvitationRepository;
import com.healthybites.identity.domain.UserRepository;
import com.healthybites.shared.config.AppProperties;
import com.healthybites.shared.security.OpaqueTokens;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Invitaciones de nutricionistas (panel de ADMIN). El enlace lleva el token en el fragmento
 * ({@code /auth/accept-invitation#token=…}) y el correo sale después del commit.
 */
@Service
public class InvitationService {

    private final NutritionistInvitationRepository invitations;
    private final UserRepository users;
    private final ApplicationEventPublisher events;
    private final AppProperties appProperties;
    private final Clock clock;
    private final Duration validity;

    public InvitationService(
            NutritionistInvitationRepository invitations,
            UserRepository users,
            ApplicationEventPublisher events,
            AppProperties appProperties,
            Clock clock,
            AuthProperties properties) {
        this.invitations = invitations;
        this.users = users;
        this.events = events;
        this.appProperties = appProperties;
        this.clock = clock;
        this.validity = properties.invitations().validity();
    }

    /** Una invitación y su estado en este momento. */
    public record InvitationView(NutritionistInvitation invitation, NutritionistInvitation.Status status) {}

    @Transactional
    public InvitationView invite(String email, String firstName, String lastName, UUID invitedBy) {
        Instant now = clock.instant();
        if (users.existsByEmail(email.trim())) {
            throw ApiException.conflict("email-taken", "Ya existe una cuenta con ese correo");
        }
        if (invitations.existsPendingFor(email.trim(), now)) {
            throw ApiException.conflict(
                    "invitation-pending", "Ya hay una invitación pendiente para ese correo: reenvíala si hace falta");
        }
        String token = OpaqueTokens.generate();
        NutritionistInvitation invitation = invitations.save(NutritionistInvitation.issue(
                email, firstName, lastName, OpaqueTokens.hash(token), invitedBy, now.plus(validity)));
        announce(invitation, token);
        return view(invitation, now);
    }

    @Transactional(readOnly = true)
    public Page<InvitationView> list(Pageable pageable) {
        Instant now = clock.instant();
        return invitations.findAllBy(pageable).map(invitation -> view(invitation, now));
    }

    /** Token nuevo (el enlace anterior deja de valer) y caducidad desde ahora. */
    @Transactional
    public InvitationView resend(UUID id) {
        Instant now = clock.instant();
        NutritionistInvitation invitation = find(id);
        NutritionistInvitation.Status status = invitation.statusAt(now);
        if (status == NutritionistInvitation.Status.ACCEPTED || status == NutritionistInvitation.Status.REVOKED) {
            throw ApiException.conflict("invitation-closed", "Esta invitación ya no se puede reenviar");
        }
        String token = OpaqueTokens.generate();
        invitation.reissue(OpaqueTokens.hash(token), now.plus(validity));
        announce(invitation, token);
        return view(invitation, now);
    }

    @Transactional
    public void revoke(UUID id) {
        Instant now = clock.instant();
        NutritionistInvitation invitation = find(id);
        if (invitation.statusAt(now) == NutritionistInvitation.Status.ACCEPTED) {
            throw ApiException.conflict("invitation-closed", "La invitación ya se aceptó");
        }
        invitation.revoke(now);
    }

    private NutritionistInvitation find(UUID id) {
        return invitations
                .findById(id)
                .orElseThrow(() -> ApiException.notFound("invitation-not-found", "La invitación no existe"));
    }

    private void announce(NutritionistInvitation invitation, String token) {
        String link = appProperties.frontend("auth/accept-invitation") + "#token=" + token;
        events.publishEvent(new InvitationIssued(invitation.getEmail(), invitation.getFirstName(), link, validity));
    }

    private static InvitationView view(NutritionistInvitation invitation, Instant now) {
        return new InvitationView(invitation, invitation.statusAt(now));
    }
}
