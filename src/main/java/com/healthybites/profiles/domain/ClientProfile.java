package com.healthybites.profiles.domain;

import com.healthybites.profiles.ClientProfiles.ClientProfileView;
import com.healthybites.profiles.Sex;
import com.healthybites.shared.domain.AuditedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Perfil de un cliente. Todos los datos son opcionales: se completan cuando el cliente quiere. */
@Entity
@Table(name = "client_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ClientProfile extends AuditedEntity {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private @Nullable Sex sex;

    @Column(name = "birth_date")
    private @Nullable LocalDate birthDate;

    @Column(name = "height_cm", precision = 5, scale = 1)
    private @Nullable BigDecimal heightCm;

    @Column(name = "weight_kg", precision = 5, scale = 1)
    private @Nullable BigDecimal weightKg;

    public static ClientProfile create(
            UUID userId,
            @Nullable Sex sex,
            @Nullable LocalDate birthDate,
            @Nullable BigDecimal heightCm,
            @Nullable BigDecimal weightKg) {
        ClientProfile profile = new ClientProfile();
        profile.userId = userId;
        profile.update(sex, birthDate, heightCm, weightKg);
        return profile;
    }

    public void update(
            @Nullable Sex sex,
            @Nullable LocalDate birthDate,
            @Nullable BigDecimal heightCm,
            @Nullable BigDecimal weightKg) {
        this.sex = sex;
        this.birthDate = birthDate;
        this.heightCm = heightCm;
        this.weightKg = weightKg;
    }

    public @Nullable Integer ageOn(LocalDate today) {
        return birthDate == null ? null : Period.between(birthDate, today).getYears();
    }

    public ClientProfileView toView() {
        return new ClientProfileView(userId, sex, birthDate, heightCm, weightKg);
    }
}
