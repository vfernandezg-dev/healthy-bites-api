package com.healthybites.profiles.app;

import com.healthybites.identity.Registrations;
import com.healthybites.identity.Registrations.InvitationPreview;
import com.healthybites.identity.Role;
import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.profiles.Sex;
import com.healthybites.profiles.domain.ClientProfile;
import com.healthybites.profiles.domain.ClientProfileRepository;
import com.healthybites.profiles.domain.NutritionistProfile;
import com.healthybites.profiles.domain.NutritionistProfileRepository;
import com.healthybites.shared.web.ApiException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;

/**
 * Registro de clientes, alta de nutricionistas por invitación y el perfil del usuario actual. La cuenta (identity)
 * y el perfil se crean en la misma transacción: si uno falla, no queda ninguno.
 */
@Service
public class ProfileService {

    private final Registrations registrations;
    private final UserAccounts accounts;
    private final ClientProfileRepository clients;
    private final NutritionistProfileRepository nutritionists;
    private final RegistrationRateLimiter rateLimiter;
    private final TransactionOperations transactions;
    private final Clock clock;

    public ProfileService(
            Registrations registrations,
            UserAccounts accounts,
            ClientProfileRepository clients,
            NutritionistProfileRepository nutritionists,
            RegistrationRateLimiter rateLimiter,
            TransactionOperations transactions,
            Clock clock) {
        this.registrations = registrations;
        this.accounts = accounts;
        this.clients = clients;
        this.nutritionists = nutritionists;
        this.rateLimiter = rateLimiter;
        this.transactions = transactions;
        this.clock = clock;
    }

    /** El usuario actual con el perfil de su rol (ninguno para ADMIN). */
    public record Me(
            UserAccount account,
            @Nullable ClientProfile client,
            @Nullable NutritionistProfile nutritionist,
            LocalDate today) {}

    public record ClientData(
            @Nullable Sex sex,
            @Nullable LocalDate birthDate,
            @Nullable BigDecimal heightCm,
            @Nullable BigDecimal weightKg) {}

    /** El límite se comprueba antes de abrir la transacción (ver {@code RateLimiter}); cuenta y perfil van juntos. */
    public Me registerClient(
            String email, String password, String firstName, String lastName, ClientData data, String clientIp) {
        rateLimiter.check(clientIp);
        return Objects.requireNonNull(transactions.execute(status -> {
            UserAccount account = registrations.registerClient(email, password, firstName, lastName);
            ClientProfile profile = clients.save(
                    ClientProfile.create(account.id(), data.sex(), data.birthDate(), data.heightCm(), data.weightKg()));
            return new Me(account, profile, null, today());
        }));
    }

    @Transactional(readOnly = true)
    public InvitationPreview previewInvitation(String token) {
        return registrations.previewInvitation(token);
    }

    @Transactional
    public Me acceptInvitation(String token, String password, @Nullable String bio) {
        UserAccount account = registrations.acceptInvitation(token, password);
        NutritionistProfile profile = nutritionists.save(NutritionistProfile.create(account.id(), bio));
        return new Me(account, null, profile, today());
    }

    @Transactional(readOnly = true)
    public Me me(UUID userId) {
        UserAccount account = accounts.findById(userId)
                .orElseThrow(() -> ApiException.notFound("user-not-found", "La cuenta no existe"));
        return switch (account.role()) {
            case CLIENT -> new Me(account, clients.findById(userId).orElse(null), null, today());
            case NUTRITIONIST ->
                new Me(account, null, nutritionists.findById(userId).orElse(null), today());
            case ADMIN -> new Me(account, null, null, today());
        };
    }

    /** Sustituye los datos editables del perfil; los de otro rol se ignoran. */
    @Transactional
    public Me update(UUID userId, String firstName, String lastName, ClientData data, @Nullable String bio) {
        UserAccount account = accounts.rename(userId, firstName, lastName);
        if (account.role() == Role.CLIENT) {
            ClientProfile profile = clients.findById(userId)
                    .orElseGet(() -> clients.save(ClientProfile.create(userId, null, null, null, null)));
            profile.update(data.sex(), data.birthDate(), data.heightCm(), data.weightKg());
        } else if (account.role() == Role.NUTRITIONIST) {
            nutritionists
                    .findById(userId)
                    .orElseGet(() -> nutritionists.save(NutritionistProfile.create(userId, null)))
                    .updateBio(bio);
        }
        return me(userId);
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
