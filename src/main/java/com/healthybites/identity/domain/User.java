package com.healthybites.identity.domain;

import com.healthybites.identity.UserAccount;
import com.healthybites.shared.domain.BaseEntity;
import com.healthybites.shared.security.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

/** Cuenta de usuario. El correo se guarda en minúsculas y es único. */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Column(nullable = false, length = 254)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "first_name", nullable = false, length = 60)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 60)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "last_login_at")
    private @Nullable Instant lastLoginAt;

    private User(String email, String passwordHash, Role role, String firstName, String lastName) {
        this.email = normalizeEmail(email);
        this.passwordHash = passwordHash;
        this.role = role;
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
        this.status = UserStatus.ACTIVE;
    }

    public static User create(String email, String passwordHash, Role role, String firstName, String lastName) {
        return new User(email, passwordHash, role, firstName, lastName);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public void recordLogin(Instant at) {
        this.lastLoginAt = at;
    }

    public void changePassword(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
    }

    public void rename(String firstName, String lastName) {
        this.firstName = firstName.trim();
        this.lastName = lastName.trim();
    }

    public void deactivate() {
        this.status = UserStatus.DEACTIVATED;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    public UserAccount toAccount() {
        return new UserAccount(Objects.requireNonNull(getId()), email, firstName, lastName, role, isActive());
    }
}
