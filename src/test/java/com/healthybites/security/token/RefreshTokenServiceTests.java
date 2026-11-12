package com.healthybites.security.token;

import com.healthybites.config.properties.RefreshTokenProperties;
import com.healthybites.model.entity.RefreshToken;
import com.healthybites.model.entity.Usuario;
import com.healthybites.repository.RefreshTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTests {

    private static final Instant T0 = Instant.parse("2026-11-12T10:00:00Z");
    private static final RefreshTokenProperties PROPERTIES = new RefreshTokenProperties(
            Duration.ofDays(7), Duration.ofDays(30), Duration.ofSeconds(20), "hb_rt", true, "/api/v1/auth");

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final MutableClock clock = new MutableClock(T0);
    private final RefreshTokenService service = new RefreshTokenService(repository, PROPERTIES, clock);
    private final Usuario usuario = new Usuario();
    private final UUID family = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        usuario.setId(4);
        when(repository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            ReflectionTestUtils.setField(token, "id", 100L);
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
        assertThat(current.isRevoked()).isTrue();
        assertThat(current.getReplacedById()).isEqualTo(100L);
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
    void expiredAndUnknownTokensAreRejectedWithoutIssuingNewOnes() {
        stored("caducado", T0.plus(Duration.ofDays(7)));
        clock.advance(Duration.ofDays(8));
        when(repository.findByTokenHash(RefreshTokenService.hash("desconocido"))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotate("caducado")).isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> service.rotate("desconocido")).isInstanceOf(InvalidRefreshTokenException.class);
        assertThatThrownBy(() -> service.rotate(null)).isInstanceOf(InvalidRefreshTokenException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void onlyTheHashIsStored() {
        RefreshTokenService.IssuedRefreshToken issued = service.issue(usuario);

        verify(repository).save(org.mockito.ArgumentMatchers.argThat(token ->
                token.getTokenHash().equals(RefreshTokenService.hash(issued.value()))
                        && !token.getTokenHash().contains(issued.value())));
    }

    private RefreshToken stored(String raw, Instant expiresAt) {
        return stored(raw, expiresAt, T0.plus(Duration.ofDays(30)));
    }

    private RefreshToken stored(String raw, Instant expiresAt, Instant familyExpiresAt) {
        RefreshToken token = RefreshToken.issue(usuario, family, RefreshTokenService.hash(raw), T0, expiresAt,
                familyExpiresAt);
        ReflectionTestUtils.setField(token, "id", 1L);
        when(repository.findByTokenHash(RefreshTokenService.hash(raw))).thenReturn(Optional.of(token));
        return token;
    }

    private static void markRotated(RefreshToken token, Instant at) {
        ReflectionTestUtils.setField(token, "revokedAt", at);
        ReflectionTestUtils.setField(token, "replacedById", 2L);
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        void set(Instant instant) {
            now = instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
