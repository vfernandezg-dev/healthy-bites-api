package com.healthybites.identity.web;

import com.healthybites.identity.Role;
import com.healthybites.identity.app.AdminUserService;
import com.healthybites.identity.domain.UserStatus;
import com.healthybites.identity.web.AdminDtos.AdminUserResponse;
import com.healthybites.identity.web.AdminDtos.UserSummaryResponse;
import com.healthybites.shared.security.CurrentUser;
import com.healthybites.shared.web.Sorting;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Set;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@Tag(name = "Admin · Cuentas", description = "Búsqueda, resumen y activación de cuentas (solo ADMIN)")
class AdminUsersController {

    private static final Set<String> SORTABLE = Set.of("createdAt", "email", "lastName", "lastLoginAt");

    private final AdminUserService adminUsers;

    AdminUsersController(AdminUserService adminUsers) {
        this.adminUsers = adminUsers;
    }

    @Operation(summary = "Busca cuentas por rol, estado y texto (nombre o correo)")
    @GetMapping
    PagedModel<AdminUserResponse> search(
            @RequestParam(required = false) @Nullable Role role,
            @RequestParam(required = false) @Nullable UserStatus status,
            @RequestParam(required = false) @Nullable String q,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return new PagedModel<>(adminUsers
                .search(role, status, q, Sorting.restrict(pageable, SORTABLE))
                .map(AdminUserResponse::of));
    }

    @Operation(summary = "Totales por rol y estado y altas de los últimos 30 días")
    @GetMapping("/summary")
    UserSummaryResponse summary() {
        return UserSummaryResponse.of(adminUsers.summary());
    }

    @Operation(summary = "Desactiva una cuenta y cierra sus sesiones")
    @PostMapping("/{id}/deactivate")
    AdminUserResponse deactivate(@PathVariable UUID id) {
        return AdminUserResponse.of(adminUsers.deactivate(id, CurrentUser.id()));
    }

    @Operation(summary = "Reactiva una cuenta")
    @PostMapping("/{id}/activate")
    AdminUserResponse activate(@PathVariable UUID id) {
        return AdminUserResponse.of(adminUsers.activate(id));
    }
}
