package com.healthybites.identity.app;

import com.healthybites.identity.Registrations;
import com.healthybites.identity.Role;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.domain.NutritionistInvitation;
import com.healthybites.identity.domain.NutritionistInvitationRepository;
import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import com.healthybites.identity.events.UserRegistered;
import com.healthybites.shared.security.OpaqueTokens;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Altas de cuentas: clientes por registro público y nutricionistas por invitación. */
@Service
class RegistrationService implements Registrations {

    private static final String INVALID_INVITATION =
            "La invitación no es válida o ha caducado. Pide al equipo que te envíe una nueva.";

    private final UserRepository users;
    private final NutritionistInvitationRepository invitations;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    RegistrationService(
            UserRepository users,
            NutritionistInvitationRepository invitations,
            PasswordEncoder passwordEncoder,
            ApplicationEventPublisher events,
            Clock clock) {
        this.users = users;
        this.invitations = invitations;
        this.passwordEncoder = passwordEncoder;
        this.events = events;
        this.clock = clock;
    }

    @Override
    @Transactional
    public UserAccount registerClient(String email, String rawPassword, String firstName, String lastName) {
        return create(email, rawPassword, Role.CLIENT, firstName, lastName);
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationPreview previewInvitation(String token) {
        NutritionistInvitation invitation = usableInvitation(token);
        return new InvitationPreview(invitation.getEmail(), invitation.getFirstName(), invitation.getLastName());
    }

    @Override
    @Transactional
    public UserAccount acceptInvitation(String token, String rawPassword) {
        NutritionistInvitation invitation = usableInvitation(token);
        UserAccount account = create(
                invitation.getEmail(),
                rawPassword,
                Role.NUTRITIONIST,
                invitation.getFirstName(),
                invitation.getLastName());
        invitation.accept(account.id(), clock.instant());
        return account;
    }

    private UserAccount create(String email, String rawPassword, Role role, String firstName, String lastName) {
        if (users.existsByEmail(email.trim())) {
            throw ApiException.conflict("email-taken", "Ya existe una cuenta con ese correo");
        }
        String hash = Objects.requireNonNull(passwordEncoder.encode(rawPassword));
        User user = users.save(User.create(email, hash, role, firstName, lastName));
        events.publishEvent(new UserRegistered(user.requireId(), role));
        return user.toAccount();
    }

    private NutritionistInvitation usableInvitation(String token) {
        Instant now = clock.instant();
        return invitations
                .findByTokenHash(OpaqueTokens.hash(token))
                .filter(invitation -> invitation.isUsableAt(now))
                .orElseThrow(() -> ApiException.badRequest("invalid-invitation", INVALID_INVITATION));
    }
}
