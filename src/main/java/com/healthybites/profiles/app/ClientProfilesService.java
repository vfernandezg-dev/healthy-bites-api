package com.healthybites.profiles.app;

import com.healthybites.profiles.ClientProfiles;
import com.healthybites.profiles.domain.ClientProfile;
import com.healthybites.profiles.domain.ClientProfileRepository;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class ClientProfilesService implements ClientProfiles {

    private final ClientProfileRepository clients;

    ClientProfilesService(ClientProfileRepository clients) {
        this.clients = clients;
    }

    @Override
    public Optional<ClientProfileView> find(UUID userId) {
        return clients.findById(userId).map(ClientProfile::toView);
    }

    @Override
    public Map<UUID, ClientProfileView> findAll(Collection<UUID> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        return clients.findAllById(userIds).stream()
                .map(ClientProfile::toView)
                .collect(Collectors.toUnmodifiableMap(ClientProfileView::userId, view -> view));
    }
}
