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
import org.jspecify.annotations.Nullable;

@Entity
@Table(name = "community_groups")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CommunityGroup extends BaseEntity {

    public enum Visibility {
        PUBLIC,
        PRIVATE
    }

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 500)
    private @Nullable String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Visibility visibility;

    public static CommunityGroup create(
            UUID ownerId, String name, @Nullable String description, Visibility visibility) {
        CommunityGroup group = new CommunityGroup();
        group.ownerId = ownerId;
        group.update(name, description, visibility);
        return group;
    }

    public void update(String name, @Nullable String description, Visibility visibility) {
        this.name = name.trim();
        this.description = description == null || description.isBlank() ? null : description.trim();
        this.visibility = visibility;
    }

    public boolean isPublic() {
        return visibility == Visibility.PUBLIC;
    }

    public boolean isOwnedBy(UUID userId) {
        return ownerId.equals(userId);
    }
}
