package com.healthybites.profiles.domain;

import com.healthybites.shared.domain.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Perfil público de un nutricionista. */
@Entity
@Table(name = "nutritionist_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NutritionistProfile extends AuditedEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 2000)
    private @Nullable String bio;

    public static NutritionistProfile create(UUID userId, @Nullable String bio) {
        NutritionistProfile profile = new NutritionistProfile();
        profile.userId = userId;
        profile.updateBio(bio);
        return profile;
    }

    public void updateBio(@Nullable String bio) {
        this.bio = bio == null || bio.isBlank() ? null : bio.trim();
    }
}
