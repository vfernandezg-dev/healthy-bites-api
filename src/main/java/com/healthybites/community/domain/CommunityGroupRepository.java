package com.healthybites.community.domain;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityGroupRepository
        extends JpaRepository<CommunityGroup, UUID>, JpaSpecificationExecutor<CommunityGroup> {}
