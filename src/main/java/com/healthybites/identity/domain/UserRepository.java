package com.healthybites.identity.domain;

import com.healthybites.identity.Role;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    /** Sin distinguir mayúsculas; usa el índice único sobre {@code lower(email)}. */
    @Query("select u from User u where lower(u.email) = lower(:email)")
    Optional<User> findByEmail(@Param("email") String email);

    @Query("select count(u) > 0 from User u where lower(u.email) = lower(:email)")
    boolean existsByEmail(@Param("email") String email);

    /** Cuentas por rol y estado, para el resumen del panel de administración. */
    @Query("select u.role as role, u.status as status, count(u) as total from User u group by u.role, u.status")
    List<RoleStatusCount> countByRoleAndStatus();

    long countByCreatedAtAfter(Instant since);

    interface RoleStatusCount {
        Role getRole();

        UserStatus getStatus();

        long getTotal();
    }
}
