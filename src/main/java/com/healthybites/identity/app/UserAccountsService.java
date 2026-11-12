package com.healthybites.identity.app;

import com.healthybites.identity.UserAccount;
import com.healthybites.identity.UserAccounts;
import com.healthybites.identity.domain.User;
import com.healthybites.identity.domain.UserRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
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
}
