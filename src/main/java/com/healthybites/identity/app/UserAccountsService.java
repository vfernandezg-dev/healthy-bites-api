package com.healthybites.identity.app;

import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import com.healthybites.shared.web.ApiException;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class UserAccountsService implements UserAccounts {

    private final UserRepository users;

    UserAccountsService(UserRepository users) {
        this.users = users;
    }

    @Override
    public Optional<UserAccount> findById(UUID id) {
        return users.findById(id).map(User::toAccount);
    }

    @Override
    public Map<UUID, UserAccount> findAllById(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return users.findAllById(ids).stream()
                .map(User::toAccount)
                .collect(Collectors.toUnmodifiableMap(UserAccount::id, account -> account));
    }

    @Override
    public Set<UUID> matching(Collection<UUID> ids, String text) {
        if (ids.isEmpty() || text.isBlank()) {
            return Set.of();
        }
        String escaped = text.trim()
                .toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return Set.copyOf(users.findIdsMatching(ids, "%" + escaped + "%"));
    }

    @Override
    @Transactional
    public UserAccount rename(UUID id, String firstName, String lastName) {
        User user =
                users.findById(id).orElseThrow(() -> ApiException.notFound("user-not-found", "La cuenta no existe"));
        user.rename(firstName, lastName);
        return user.toAccount();
    }
}
