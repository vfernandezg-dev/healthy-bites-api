package com.healthybites.community.domain;

import com.healthybites.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "group_memberships")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMembership extends BaseEntity {

    public enum Role {
        OWNER,
        MEMBER
    }

    public enum Status {
        PENDING,
        ACTIVE
    }

    @Column(name = "group_id", nullable = false)
    private UUID groupId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Status status;

    public static GroupMembership of(UUID groupId, UUID userId, Role role, Status status) {
        GroupMembership membership = new GroupMembership();
        membership.groupId = groupId;
        membership.userId = userId;
        membership.role = role;
        membership.status = status;
        return membership;
    }

    public void approve() {
        this.status = Status.ACTIVE;
    }

    public boolean isActive() {
        return status == Status.ACTIVE;
    }
}
