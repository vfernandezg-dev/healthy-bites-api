package com.healthybites.identity;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.healthybites.IntegrationTest;
import com.healthybites.support.Seed;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

/** Panel de ADMIN: búsqueda paginada, resumen y desactivación de cuentas. */
class AdminUsersTests extends IntegrationTest {

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${healthybites.security.refresh.cookie-name}")
    private String cookieName;

    @Test
    void searchFiltersByRoleAndTextAndPaginates() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("role", "NUTRITIONIST")
                        .param("size", "1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].role").value("NUTRITIONIST"))
                .andExpect(jsonPath("$.page.size").value(1))
                .andExpect(jsonPath("$.page.totalElements", greaterThanOrEqualTo(2)));

        mockMvc.perform(get("/admin/users")
                        .param("q", "pérez")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", hasItem(Seed.JUAN)));

        mockMvc.perform(get("/admin/users")
                        .param("status", "ACTIVE")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(jsonPath("$.content[*].status", everyItem(is("ACTIVE"))));
    }

    @Test
    void sortingIsLimitedToAWhitelist() throws Exception {
        mockMvc.perform(get("/admin/users")
                        .param("sort", "passwordHash")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("invalid-sort"));
    }

    @Test
    void summaryCountsAccountsByRoleAndStatus() throws Exception {
        mockMvc.perform(get("/admin/users/summary").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.byRole.ADMIN", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.byRole.NUTRITIONIST", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.byRole.CLIENT", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.total", greaterThanOrEqualTo(6)));
    }

    @Test
    void deactivatingAnAccountBlocksLoginAndEndsItsSessions() throws Exception {
        UUID id = createClient("baja");
        String email = jdbc.queryForObject("select email from users where id = ?", String.class, id);
        String setCookie = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, Seed.PASSWORD)))
                .andReturn()
                .getResponse()
                .getHeader(HttpHeaders.SET_COOKIE);
        String cookie = setCookie.substring(cookieName.length() + 1, setCookie.indexOf(';'));

        mockMvc.perform(post("/admin/users/" + id + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DEACTIVATED"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, Seed.PASSWORD)))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie(cookieName, cookie))
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/admin/users/" + id + "/activate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, Seed.PASSWORD)))
                .andExpect(status().isOk());
    }

    @Test
    void anAdminCannotDeactivateThemselves() throws Exception {
        mockMvc.perform(post("/admin/users/" + Seed.ADMIN_ID + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.ADMIN))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("cannot-deactivate-self"));
    }

    @Test
    void onlyAdminsUseThePanel() throws Exception {
        mockMvc.perform(get("/admin/users").header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.CARLOS))))
                .andExpect(status().isForbidden());
        mockMvc.perform(post("/admin/users/" + Seed.JUAN_ID + "/deactivate")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token(Seed.MARIA))))
                .andExpect(status().isForbidden());
    }

    private UUID createClient(String alias) {
        UUID id = UUID.randomUUID();
        jdbc.update(
                "insert into users (id, email, password_hash, role, first_name, last_name) values (?, ?, ?, 'CLIENT', ?, ?)",
                id,
                alias + "-" + id.toString().substring(0, 8) + "@example.com",
                passwordEncoder.encode(Seed.PASSWORD),
                "Prueba",
                alias);
        return id;
    }
}
