package com.healthybites;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Autorización (API-7): cada usuario solo ve y toca lo suyo, el acceso premium se decide en el servidor y las reglas
 * de negocio que se podían forzar desde el cliente ya no se pueden.
 *
 * <p>Datos del seed: Juan (usuario 4, cliente 1, BÁSICO), María (5, cliente 2, PREMIUM), Pedro (6, cliente 3, VIP),
 * Carlos (2, nutricionista 1) y Elena (3, nutricionista 2). Grupos 1 y 2 públicos (de Juan y María); el 3 es privado
 * y de Pedro.
 */
class AuthorizationTests extends IntegrationTest {

    private static final String JUAN = "juan.perez@healthybites.com";
    private static final String MARIA = "maria.lopez@healthybites.com";
    private static final String PEDRO = "pedro.gomez@healthybites.com";
    private static final String CARLOS = "carlos.garcia@healthybites.com";
    private static final String ELENA = "elena.gomez@healthybites.com";
    private static final String ADMIN = "admin@healthybites.com";

    private static final Map<String, String> TOKENS = new ConcurrentHashMap<>();

    @Autowired
    private JdbcTemplate jdbc;

    record Case(String who, HttpMethod method, String url, String body, int status) {

        static Case of(String who, HttpMethod method, String url, int status) {
            return new Case(who, method, url, null, status);
        }

        @Override
        public String toString() {
            return who.substring(0, who.indexOf('.')) + " " + method + " " + url + " -> " + status;
        }
    }

    static Stream<Case> crossTenantMatrix() {
        HttpMethod GET = HttpMethod.GET;
        HttpMethod POST = HttpMethod.POST;
        HttpMethod DELETE = HttpMethod.DELETE;
        return Stream.of(
                // Datos del cliente: los suyos sí, los de otro cliente no (ni mezclando ids propios y ajenos).
                Case.of(JUAN, GET, "/meta/cliente/1", 200),
                Case.of(JUAN, GET, "/meta/cliente/2", 403),
                Case.of(JUAN, GET, "/meta/2/cliente/1", 403),
                Case.of(JUAN, DELETE, "/meta/2/cliente/2", 403),
                Case.of(JUAN, GET, "/meta/1/seguimientos", 200),
                Case.of(JUAN, GET, "/meta/2/seguimientos", 403),
                Case.of(JUAN, GET, "/meta/1/seguimientos/1", 200),
                Case.of(JUAN, GET, "/meta/1/seguimientos/7", 403),
                Case.of(JUAN, GET, "/meta/1/seguimientos/page", 403),
                Case.of(JUAN, GET, "/habito/1", 200),
                Case.of(JUAN, GET, "/habito/4", 403),
                Case.of(JUAN, DELETE, "/habito/4", 403),
                Case.of(JUAN, GET, "/habito/cliente/2", 403),
                Case.of(JUAN, GET, "/racha/2/all-recompensas", 403),
                Case.of(JUAN, GET, "/acceso-contenido/2/all-contenidos", 403),
                Case.of(JUAN, GET, "/acceso-plan/2/all-planes", 403),
                Case.of(JUAN, GET, "/user/profile/4", 200),
                Case.of(JUAN, GET, "/user/profile/5", 403),
                Case.of(JUAN, GET, "/suscripcion/1", 200),
                Case.of(JUAN, GET, "/suscripcion/2", 403),
                Case.of(JUAN, GET, "/suscripcion", 403),
                Case.of(JUAN, POST, "/checkout/create?suscripcionId=2&returnUrl=x&cancelUrl=y", 403),

                // Acceso premium en el servidor: gratis o con suscripción PREMIUM/VIP pagada.
                Case.of(JUAN, GET, "/contenido/1", 200),
                Case.of(JUAN, GET, "/contenido/2", 403),
                Case.of(MARIA, GET, "/contenido/6", 200),
                Case.of(PEDRO, GET, "/contenido/2", 200),
                Case.of(JUAN, GET, "/plan-alimenticio/3", 200),
                Case.of(JUAN, GET, "/plan-alimenticio/1", 403),
                Case.of(JUAN, GET, "/plan-alimenticio/1/comidas", 403),
                Case.of(JUAN, GET, "/plan-alimenticio/3/comidas/1", 403),
                Case.of(JUAN, POST, "/acceso-contenido/1/add-contenido?contentId=2", 403),

                // Nutricionistas: solo sobre lo suyo. Los clientes ya no escriben contenidos ni recompensas.
                Case.of(ELENA, DELETE, "/contenido/6", 403),
                Case.of(ELENA, DELETE, "/plan-alimenticio/1/nutricionista/2", 403),
                Case.of(ELENA, DELETE, "/plan-alimenticio/1/comidas/1", 403),
                Case.of(ELENA, DELETE, "/recompensa/1", 403),
                Case.of(ELENA, GET, "/plan-alimenticio/comidas/nutricionista/1", 403),
                Case.of(JUAN, DELETE, "/recompensa/1", 403),
                new Case(JUAN, POST, "/contenido", contenidoBody("Contenido de un cliente", "contenido-cliente", null), 403),

                // /admin/cliente es solo de ADMIN.
                Case.of(ELENA, GET, "/admin/cliente", 403),
                Case.of(ADMIN, GET, "/admin/cliente", 200),

                // Comunidad: los grupos privados solo los leen sus miembros; lo ajeno no se edita ni se borra.
                Case.of(PEDRO, GET, "/grupo/3", 200),
                Case.of(JUAN, GET, "/grupo/3", 403),
                Case.of(JUAN, GET, "/publicacion/grupo/3", 403),
                Case.of(JUAN, POST, "/unirse/1/add-grupo?groupId=3", 403),
                Case.of(JUAN, DELETE, "/grupo/2/cliente/1", 403),
                Case.of(JUAN, DELETE, "/publicacion/4/cliente/1", 403),
                Case.of(JUAN, DELETE, "/publicacion/4/comentario/5", 403),
                Case.of(JUAN, DELETE, "/publicacion/1/comentario/4", 403),
                Case.of(JUAN, GET, "/publicacion/cliente/2", 403));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource
    void crossTenantMatrix(Case c) throws Exception {
        MockHttpServletRequestBuilder request = request(c.method(), c.url())
                .header("Authorization", bearer(token(c.who())));
        if (c.body() != null) {
            request.contentType(MediaType.APPLICATION_JSON).content(c.body());
        }
        mockMvc.perform(request).andExpect(status().is(c.status()));
    }

    @Test
    void privateGroupPostsAreOnlyVisibleToMembers() throws Exception {
        String response = mockMvc.perform(post("/publicacion")
                        .header("Authorization", bearer(token(PEDRO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titulo": "Solo para el grupo", "descripcion": "Mensaje privado", "clienteId": 3, "grupoId": 3}
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int postId = JsonPath.read(response, "$.id");

        // Antes el feed devolvía todas las publicaciones y los comentarios no comprobaban nada.
        mockMvc.perform(get("/publicacion").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(postId))));
        mockMvc.perform(get("/grupo").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(3))));
        mockMvc.perform(get("/publicacion/" + postId + "/comentario").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/publicacion").header("Authorization", bearer(token(PEDRO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(postId)));
        mockMvc.perform(get("/grupo").header("Authorization", bearer(token(PEDRO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItem(3)));
    }

    @Test
    void memberCountOnlyChangesWhenMembershipChanges() throws Exception {
        // Pedro (cliente 3) entra y sale del grupo 1 de Juan, que empieza con 2 miembros.
        String pedro = bearer(token(PEDRO));
        mockMvc.perform(post("/unirse/3/add-grupo").param("groupId", "1").header("Authorization", pedro))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidadMiembros").value(3));
        mockMvc.perform(post("/unirse/3/add-grupo").param("groupId", "1").header("Authorization", pedro))
                .andExpect(status().isBadRequest());
        assertThat(memberCount(1)).isEqualTo(3);

        mockMvc.perform(delete("/unirse/3/remove-grupo/1").header("Authorization", pedro))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/unirse/3/remove-grupo/1").header("Authorization", pedro))
                .andExpect(status().isNoContent());
        assertThat(memberCount(1)).isEqualTo(2);

        // El creador ya cuenta como miembro: no puede unirse otra vez a su propio grupo.
        mockMvc.perform(post("/unirse/1/add-grupo").param("groupId", "1").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isBadRequest());
        assertThat(memberCount(1)).isEqualTo(2);
    }

    @Test
    void aStreakCountsAtMostOncePerDayAndResetsAfterAGap() throws Exception {
        // Racha de Juan para la recompensa 1 (7 días, desbloquea el contenido 3).
        setStreak(3, LocalDateTime.now().minusDays(1));
        registerHabit();
        assertThat(streakDays()).isEqualTo(4);
        registerHabit();
        assertThat(streakDays()).isEqualTo(4);

        setStreak(5, LocalDateTime.now().minusDays(3));
        registerHabit();
        assertThat(streakDays()).isEqualTo(1);
    }

    @Test
    void completingAStreakUnlocksItsReward() throws Exception {
        mockMvc.perform(get("/contenido/3").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isForbidden());

        setStreak(6, LocalDateTime.now().minusDays(1));
        registerHabit();

        assertThat(jdbc.queryForObject(
                "select entregada from racha where cliente_id = 1 and recompensa_id = 1", Boolean.class)).isTrue();
        mockMvc.perform(get("/contenido/3").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isOk());
    }

    @Test
    void theCatalogDoesNotHandOutTheFileOfLockedContent() throws Exception {
        String response = mockMvc.perform(post("/contenido")
                        .header("Authorization", bearer(token(CARLOS)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(contenidoBody("Guía premium de prueba", "guia-premium-prueba", "guia-premium.pdf")))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        int id = JsonPath.read(response, "$.id");
        String item = "$[?(@.id == " + id + ")].filePath";

        mockMvc.perform(get("/contenido").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(item).value(hasItem(nullValue())));
        mockMvc.perform(get("/contenido/nutricionista/1").header("Authorization", bearer(token(JUAN))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(item).value(hasItem(nullValue())));
        mockMvc.perform(get("/contenido").header("Authorization", bearer(token(MARIA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(item).value(contains("guia-premium.pdf")));
        mockMvc.perform(get("/contenido").header("Authorization", bearer(token(ELENA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath(item).value(contains("guia-premium.pdf")));
    }

    private String token(String email) throws Exception {
        String token = TOKENS.get(email);
        if (token == null) {
            token = loginAndGetToken(email);
            TOKENS.put(email, token);
        }
        return token;
    }

    private static String contenidoBody(String titulo, String slug, String filePath) {
        return """
                {"titulo": "%s", "descripcion": "Contenido de prueba", "slug": "%s", "tipoContenido": "GUIA",
                 "categoriaContenido": "NUTRICION", "filePath": %s, "esGratis": false, "nutricionistaId": 1}
                """.formatted(titulo, slug, filePath == null ? "null" : "\"" + filePath + "\"");
    }

    private Integer memberCount(int grupoId) {
        return jdbc.queryForObject("select cantidad_miembros from grupos where id = ?", Integer.class, grupoId);
    }

    private void setStreak(int days, LocalDateTime lastRecord) {
        jdbc.update("""
                update racha set contador_dias = ?, entregada = false, ultima_fecha_registro = ?
                where cliente_id = 1 and recompensa_id = 1""", days, lastRecord);
    }

    private Integer streakDays() {
        return jdbc.queryForObject(
                "select contador_dias from racha where cliente_id = 1 and recompensa_id = 1", Integer.class);
    }

    private void registerHabit() throws Exception {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        mockMvc.perform(post("/habito")
                        .header("Authorization", bearer(token(JUAN)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nombre": "Registro diario", "fechaRegistro": "%s", "hidratacion": 2.0,
                                 "alimentacion": 1800, "ejercicio": 1.0, "calidadDeSueno": 7.0, "clienteId": 1}
                                """.formatted(today)))
                .andExpect(status().isCreated());
    }
}
