package com.healthybites;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Sesión con access token corto + refresh token rotatorio en cookie (fase de seguridad, API-6).
 */
class TokenSessionTests extends IntegrationTest {

    private static final String APP_ORIGIN = "http://localhost:4210";
    private static final String LANDING_ORIGIN = "http://localhost:4321";

    @Value("${healthybites.security.refresh.cookie-name}")
    private String cookieName;

    @Autowired
    private JwtEncoder jwtEncoder;

    // ---------------------------------------------------------------- login y access token

    @Test
    void loginReturnsAShortLivedAccessTokenAndAHardenedRefreshCookie() throws Exception {
        MockHttpServletResponse response = login("maria.lopez@healthybites.com");

        assertThat((Integer) JsonPath.read(response.getContentAsString(), "$.expiresIn")).isEqualTo(900);
        assertThat((String) JsonPath.read(response.getContentAsString(), "$.tokenType")).isEqualTo("Bearer");
        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie)
                .startsWith(cookieName + "=")
                .contains("HttpOnly", "Secure", "SameSite=Strict", "Path=/api/v1/auth", "Max-Age=");
        // El refresh token nunca va en el cuerpo
        assertThat(response.getContentAsString()).doesNotContain(cookieValue(response));
    }

    @Test
    void forgedExpiredUnsignedOrForeignTokensAreRejected() throws Exception {
        Instant now = Instant.now();
        JwtClaimsSet valid = claims(now, now.plusSeconds(600), "healthy-bites-web");

        // Control: un token bien firmado funciona.
        mockMvc.perform(get("/meta/cliente/1").header("Authorization", bearer(encode(jwtEncoder, valid))))
                .andExpect(status().isOk());

        byte[] otherKey = new byte[64];
        new SecureRandom().nextBytes(otherKey);
        JwtEncoder forger = NimbusJwtEncoder.withSecretKey(new SecretKeySpec(otherKey, "HmacSHA512"))
                .algorithm(MacAlgorithm.HS512).build();
        String forged = encode(forger, valid);
        String expired = encode(jwtEncoder, claims(now.minus(Duration.ofHours(2)), now.minus(Duration.ofHours(1)),
                "healthy-bites-web"));
        String otherAudience = encode(jwtEncoder, claims(now, now.plusSeconds(600), "otra-aplicacion"));
        String unsigned = base64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}") + "."
                + base64Url("{\"sub\":\"1\",\"roles\":[\"ADMIN\"],\"iss\":\"healthy-bites-api\","
                + "\"aud\":[\"healthy-bites-web\"],\"exp\":" + now.plusSeconds(600).getEpochSecond() + "}") + ".";

        for (String token : List.of(forged, expired, otherAudience, unsigned)) {
            mockMvc.perform(get("/meta/cliente/1").header("Authorization", bearer(token)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401));
        }
    }

    // ---------------------------------------------------------------- refresh y logout

    @Test
    void refreshRotatesTheCookieAndARecentlyRotatedOneIsOnlyRejected() throws Exception {
        String first = cookieValue(login("pedro.gomez@healthybites.com"));

        MockHttpServletResponse refreshed = mockMvc.perform(refresh(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.role").value("CLIENTE"))
                .andReturn().getResponse();
        String second = cookieValue(refreshed);
        assertThat(second).isNotBlank().isNotEqualTo(first);

        // El token anterior ya no sirve. Dentro de la ventana de gracia (dos pestañas a la vez) solo se rechaza...
        mockMvc.perform(refresh(first))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));
        // ...sin cerrar la sesión: el token nuevo sigue funcionando.
        mockMvc.perform(refresh(second)).andExpect(status().isOk());
    }

    @Test
    void refreshRequiresTheCustomHeaderAndAnAllowedOrigin() throws Exception {
        String token = cookieValue(login("juan.perez@healthybites.com"));

        mockMvc.perform(post("/auth/refresh").cookie(new Cookie(cookieName, token)))
                .andExpect(status().isForbidden());
        mockMvc.perform(refresh(token).header(HttpHeaders.ORIGIN, "https://evil.example"))
                .andExpect(status().isForbidden());
        mockMvc.perform(refresh(token).header(HttpHeaders.ORIGIN, APP_ORIGIN))
                .andExpect(status().isOk());
    }

    @Test
    void refreshWithoutCookieIs401() throws Exception {
        mockMvc.perform(post("/auth/refresh").header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    void logoutRevokesTheSessionAndClearsTheCookie() throws Exception {
        String token = cookieValue(login("elena.gomez@healthybites.com"));

        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie(cookieName, token))
                        .header("X-Requested-With", "XMLHttpRequest"))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")));

        mockMvc.perform(refresh(token)).andExpect(status().isUnauthorized());
    }

    // ---------------------------------------------------------------- protección

    @Test
    void loginFailuresAreRateLimitedWithRetryAfter() throws Exception {
        for (int attempt = 0; attempt < 5; attempt++) {
            mockMvc.perform(failedLogin()).andExpect(status().isUnauthorized());
        }
        mockMvc.perform(failedLogin())
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER));
    }

    @Test
    void corsAllowsTheAppWithCredentialsAndTheLandingOnlyForContact() throws Exception {
        mockMvc.perform(preflight("/auth/login", APP_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, APP_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));

        mockMvc.perform(preflight("/contact", LANDING_ORIGIN))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LANDING_ORIGIN));

        mockMvc.perform(preflight("/auth/login", LANDING_ORIGIN)).andExpect(status().isForbidden());
        mockMvc.perform(preflight("/auth/login", "https://evil.example")).andExpect(status().isForbidden());
    }

    @Test
    void responsesCarrySecurityHeadersAndActuatorOnlyExposesHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(header().string("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"))
                .andExpect(header().string("X-Frame-Options", "DENY"));

        String adminToken = loginAndGetToken("admin@healthybites.com");
        mockMvc.perform(get("/actuator/env").header("Authorization", bearer(adminToken)))
                .andExpect(status().isNotFound());
    }

    // ---------------------------------------------------------------- utilidades

    private MockHttpServletResponse login(String email) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody(email, DEMO_PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse();
    }

    private MockHttpServletRequestBuilder refresh(String token) {
        return post("/auth/refresh")
                .cookie(new Cookie(cookieName, token))
                .header("X-Requested-With", "XMLHttpRequest");
    }

    private MockHttpServletRequestBuilder failedLogin() {
        return post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody("nadie@example.com", "contrasena-incorrecta"))
                .with(request -> {
                    request.setRemoteAddr("198.51.100.23");
                    return request;
                });
    }

    private static MockHttpServletRequestBuilder preflight(String path, String origin) {
        return options(path)
                .header(HttpHeaders.ORIGIN, origin)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Content-Type");
    }

    private String cookieValue(MockHttpServletResponse response) {
        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).isNotNull().startsWith(cookieName + "=");
        return setCookie.substring(cookieName.length() + 1, setCookie.indexOf(';'));
    }

    private static JwtClaimsSet claims(Instant issuedAt, Instant expiresAt, String audience) {
        return JwtClaimsSet.builder()
                .issuer("healthy-bites-api")
                .audience(List.of(audience))
                .subject("4")
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim("roles", List.of("CLIENTE"))
                .build();
    }

    private static String encode(JwtEncoder encoder, JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS512).type("JWT").build();
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    private static String base64Url(String json) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }
}
