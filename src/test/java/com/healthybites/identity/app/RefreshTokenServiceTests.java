package com.healthybites.identity.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.healthybites.identity.domain.RefreshToken;
import com.healthybites.identity.domain.RefreshTokenRepository;
import com.healthybites.identity.domain.User;
import com.healthybites.shared.security.OpaqueTokens;
import com.healthybites.shared.security.Role;
import com.healthybites.support.MutableClock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RefreshTokenServiceTests {

    private static final Instant T0 = Instant.parse("2026-11-12T10:00:00Z");
    private static final RefreshTokenProperties PROPERTIES = new RefreshTokenProperties(
            Duration.ofDays(7), Duration.ofDays(30), Duration.ofSeconds(20), "hb_rt", true, "/api/v1/auth");
    private static final UUID NEXT_ID = UUID.fromString("00000000-0000-7000-8000-0000000000aa");

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final MutableClock clock = new MutableClock(T0);
    private final RefreshTokenService service = new RefreshTokenService(repository, PROPERTIES, clock);
    private final User user = User.create("juan@example.com", "{noop}x", Role.CLIENT, "Juan", "Pérez");
    private final UUID family = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            ReflectionTestUtils.setField(token, "id", NEXT_ID);
            return token;
        });
    }

    @Test
    void rotatingAValidTokenRevokesItAndIssuesAnotherInTheSameSession() {
        RefreshToken current = stored("valor-actual", T0.plus(Duration.ofDays(7)));
        clock.advance(Duration.ofHours(1));

        RefreshTokenService.IssuedRefreshToken next = service.rotate("valor-actual");

        assertThat(next.value()).isNotBlank().isNotEqualTo("valor-actual");
        assertThat(next.expiresAt()).isEqualTo(clock.instant().plus(Duration.ofDays(7)));
        assertThat(current.getRevokedAt()).isNotNull();
        assertThat(current.getReplacedById()).isEqualTo(NEXT_ID);
    }

    @Test
    void aSessionNeverOutlivesItsAbsoluteLifetime() {
        // Sesión usada a diario: el token actual se rotó hace poco y vence con la sesión.
        Instant familyExpiry = T0.plus(Duration.ofDays(30));
        stored("valor-actual", familyExpiry, familyExpiry);
        clock.set(familyExpiry.minus(Duration.ofDays(2)));

        RefreshTokenService.IssuedRefreshToken next = service.rotate("valor-actual");

        assertThat(next.expiresAt()).isEqualTo(familyExpiry);
    }

    @Test
    void reusingARotatedTokenAfterTheGraceWindowRevokesTheWholeSession() {
        RefreshToken current = stored("robado", T0.plus(Duration.ofDays(7)));
        markRotated(current, T0);
        clock.advance(Duration.ofSeconds(30));

        assertThatThrownBy(() -> service.rotate("robado")).isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository).revokeFamily(family, clock.instant());
    }

    @Test
    void reusingARotatedTokenWithinTheGraceWindowIsOnlyRejected() {
        RefreshToken current = stored("dos-pestanas", T0.plus(Duration.ofDays(7)));
        markRotated(current, T0);
        clock.advance(Duration.ofSeconds(5));

        assertThatThrownBy(() -> service.rotate("dos-pestanas")).isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository, never()).revokeFamily(any(), any());
    }

    @Test
    void expiredUnknownAndMissingTokensAreRejectedWithoutIssuingNewOnes() {
        stored("caducado", T0.plus(Duration.ofDays(7)));
        clock.advance(Duration.ofDays(8));
        when(repository.findByTokenHash(OpaqueTokens.hash("desconocido"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotate("caducado")).isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> service.rotate("desconocido")).isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> service.rotate(null)).isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void aDeactivatedAccountCannotRenewItsSession() {
        stored("cuenta-desactivada", T0.plus(Duration.ofDays(7)));
        user.deactivate();

        assertThatThrownBy(() -> service.rotate("cuenta-desactivada")).isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository).revokeFamily(family, clock.instant());
    }

    @Test
    void onlyTheHashIsStored() {
        RefreshTokenService.IssuedRefreshToken issued = service.issue(user);

        verify(repository)
                .save(argThat(token -> token.getTokenHash().equals(OpaqueTokens.hash(issued.value()))
                        && !token.getTokenHash().contains(issued.value())));
    }

    private RefreshToken stored(String raw, Instant expiresAt) {
        return stored(raw, expiresAt, T0.plus(Duration.ofDays(30)));
    }

    private RefreshToken stored(String raw, Instant expiresAt, Instant familyExpiresAt) {
        RefreshToken token = RefreshToken.issue(user, family, OpaqueTokens.hash(raw), T0, expiresAt, familyExpiresAt);
        ReflectionTestUtils.setField(token, "id", UUID.randomUUID());
        when(repository.findByTokenHash(OpaqueTokens.hash(raw))).thenReturn(Optional.of(token));
        return token;
    }

    private static void markRotated(RefreshToken token, Instant at) {
        ReflectionTestUtils.setField(token, "revokedAt", at);
        ReflectionTestUtils.setField(token, "replacedById", UUID.randomUUID());
    }
}
