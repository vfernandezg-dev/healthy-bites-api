package com.healthybites.community.domain;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMembershipRepository extends JpaRepository<GroupMembership, UUID> {

    Optional<GroupMembership> findByGroupIdAndUserId(UUID groupId, UUID userId);

    List<GroupMembership> findByGroupIdOrderByCreatedAtAsc(UUID groupId);

    List<GroupMembership> findByUserIdAndGroupIdIn(UUID userId, Collection<UUID> groupIds);

    /** Miembros activos por grupo (el contador se calcula, no se guarda). */
    @Query("""
            select m.groupId as groupId, count(m) as total from GroupMembership m
            where m.groupId in :groupIds
              and m.status = com.healthybites.community.domain.GroupMembership.Status.ACTIVE
            group by m.groupId""")
    List<GroupCount> countActive(@Param("groupIds") Collection<UUID> groupIds);

    interface GroupCount {
        UUID getGroupId();

        long getTotal();
    }
}
