package com.healthybites.profiles.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, UUID> {}
