package com.healthybites.identity.app;

import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import com.healthybites.identity.domain.UserStatus;
import com.healthybites.identity.events.UserDeactivated;
import com.healthybites.shared.persistence.TextSearch;
import com.healthybites.shared.security.Role;
import com.healthybites.shared.web.ApiException;
import java.time.Clock;
import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Gestión de cuentas desde el panel de ADMIN: búsqueda, resumen y activación. */
@Service
public class AdminUserService {

    private final UserRepository users;
    private final RefreshTokenService refreshTokens;
    private final ApplicationEventPublisher events;
    private final Clock clock;

    public AdminUserService(
            UserRepository users, RefreshTokenService refreshTokens, ApplicationEventPublisher events, Clock clock) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.events = events;
        this.clock = clock;
    }

    public record Summary(long total, long active, long deactivated, Map<Role, Long> byRole, long newLast30Days) {}

    @Transactional(readOnly = true)
    public Page<User> search(
            @Nullable Role role, @Nullable UserStatus status, @Nullable String query, Pageable pageable) {
        Specification<User> spec = Specification.unrestricted();
        if (role != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("role"), role));
        }
        if (status != null) {
            spec = spec.and((root, q, cb) -> cb.equal(root.get("status"), status));
        }
        if (query != null && !query.isBlank()) {
            // El nombre completo ("Juan Pérez") se busca concatenado: TextSearch solo aporta el patrón escapado.
            String like = TextSearch.containsPattern(query);
            spec = spec.and((root, q, cb) -> cb.or(
                    cb.like(cb.lower(root.get("email")), like, TextSearch.ESCAPE),
                    cb.like(
                            cb.lower(cb.concat(cb.concat(root.get("firstName"), " "), root.get("lastName"))),
                            like,
                            TextSearch.ESCAPE)));
        }
        return users.findAll(spec, pageable);
    }

    @Transactional(readOnly = true)
    public Summary summary() {
        Map<Role, Long> byRole = new EnumMap<>(Role.class);
        for (Role role : Role.values()) {
            byRole.put(role, 0L);
        }
        long active = 0;
        long deactivated = 0;
        for (UserRepository.RoleStatusCount row : users.countByRoleAndStatus()) {
            byRole.merge(row.getRole(), row.getTotal(), Long::sum);
            if (row.getStatus() == UserStatus.ACTIVE) {
                active += row.getTotal();
            } else {
                deactivated += row.getTotal();
            }
        }
        long recent = users.countByCreatedAtAfter(clock.instant().minus(Duration.ofDays(30)));
        return new Summary(active + deactivated, active, deactivated, byRole, recent);
    }

    /** Bloquea la cuenta y cierra sus sesiones (los access tokens ya emitidos caducan en 15 minutos). */
    @Transactional
    public User deactivate(UUID id, UUID adminId) {
        if (id.equals(adminId)) {
            throw ApiException.conflict("cannot-deactivate-self", "No puedes desactivar tu propia cuenta");
        }
        User user = find(id);
        if (user.isActive()) {
            user.deactivate();
            refreshTokens.revokeAllSessions(id);
            events.publishEvent(new UserDeactivated(id));
        }
        return user;
    }

    @Transactional
    public User activate(UUID id) {
        User user = find(id);
        user.activate();
        return user;
    }

    private User find(UUID id) {
        return users.findById(id).orElseThrow(() -> ApiException.notFound("user-not-found", "La cuenta no existe"));
    }
}
