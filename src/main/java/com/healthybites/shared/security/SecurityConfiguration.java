package com.healthybites.shared.security;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Seguridad de la API: sin estado y autenticada con access tokens JWT (resource server de Spring Security).
 *
 * <ul>
 *   <li>Roles por prefijo de ruta: {@code /admin/**} solo ADMIN y {@code /nutritionist/**} solo NUTRITIONIST.</li>
 *   <li>La propiedad de cada recurso se comprueba en los servicios: un recurso ajeno responde 404.</li>
 *   <li>Las rutas públicas las declara cada módulo con un bean {@link PublicEndpoints}.</li>
 * </ul>
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfiguration {

    /** Cabecera que exigen los endpoints que se autentican con la cookie del refresh token (defensa CSRF). */
    public static final String REQUESTED_WITH = "X-Requested-With";

    /** Swagger UI (solo existe en el perfil dev): necesita scripts y estilos, así que no lleva la CSP estricta. */
    @Bean
    @Order(1)
    SecurityFilterChain apiDocsSecurityFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/v3/api-docs/**", "/v3/api-docs.yaml", "/swagger-ui/**", "/swagger-ui.html")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(2)
    SecurityFilterChain apiSecurityFilterChain(
            HttpSecurity http,
            List<PublicEndpoints> publicEndpoints,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            SecurityProblemHandlers problemHandlers)
            throws Exception {
        RequestMatcher publicRequests = publicRequests(publicEndpoints);
        http.cors(cors -> {})
                // Sin estado y sin cookies de autenticación: el CSRF de las rutas con la cookie del refresh token se
                // defiende con SameSite=Strict, la cabecera X-Requested-With y el Origin (ver identity).
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/error")
                        .permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**")
                        .permitAll()
                        .requestMatchers(publicRequests)
                        .permitAll()
                        .requestMatchers("/admin/**")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers("/nutritionist/**")
                        .hasRole(Role.NUTRITIONIST.name())
                        .anyRequest()
                        .authenticated())
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .bearerTokenResolver(ignoringPublicEndpoints(publicRequests))
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                        .authenticationEntryPoint(problemHandlers)
                        .accessDeniedHandler(problemHandlers))
                .exceptionHandling(exceptions ->
                        exceptions.authenticationEntryPoint(problemHandlers).accessDeniedHandler(problemHandlers))
                .headers(headers -> headers
                        // La API solo devuelve JSON y ficheros: nada debe ejecutarse ni incrustarse.
                        .contentSecurityPolicy(
                                csp -> csp.policyDirectives("default-src 'none'; frame-ancestors 'none'"))
                        .referrerPolicy(referrer -> referrer.policy(ReferrerPolicy.NO_REFERRER))
                        .frameOptions(frame -> frame.deny()));
        return http.build();
    }

    /**
     * CORS por listas blancas: la landing solo puede llamar a {@code POST /contact}, sin credenciales; el web-app,
     * a toda la API con credenciales (la cookie del refresh token).
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(CorsProperties cors) {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Se registra antes que /**: gana la primera coincidencia.
        if (!cors.landingOrigins().isEmpty()) {
            CorsConfiguration landing = new CorsConfiguration();
            landing.setAllowedOrigins(cors.landingOrigins());
            landing.setAllowedMethods(List.of("POST"));
            landing.setAllowedHeaders(List.of(HttpHeaders.CONTENT_TYPE));
            landing.setExposedHeaders(List.of(HttpHeaders.RETRY_AFTER));
            landing.setMaxAge(Duration.ofHours(1));
            source.registerCorsConfiguration("/contact", landing);
        }
        if (!cors.appOrigins().isEmpty()) {
            CorsConfiguration app = new CorsConfiguration();
            app.setAllowedOrigins(cors.appOrigins());
            app.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
            app.setAllowedHeaders(List.of(HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, REQUESTED_WITH));
            app.setExposedHeaders(List.of(HttpHeaders.RETRY_AFTER, HttpHeaders.LOCATION, "X-Request-Id"));
            app.setAllowCredentials(true);
            app.setMaxAge(Duration.ofHours(1));
            source.registerCorsConfiguration("/**", app);
        }
        return source;
    }

    /**
     * Codificador delegante: los hashes llevan prefijo ({@code {bcrypt}...}) para poder cambiar de algoritmo más
     * adelante sin invalidar las contraseñas existentes.
     */
    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private static RequestMatcher publicRequests(List<PublicEndpoints> publicEndpoints) {
        List<RequestMatcher> matchers = publicEndpoints.stream()
                .flatMap(group -> group.endpoints().stream())
                .map(PublicEndpoints.Endpoint::matcher)
                .toList();
        return matchers.isEmpty() ? request -> false : new OrRequestMatcher(matchers);
    }

    /** En las rutas públicas se ignora el bearer: uno caducado no debe impedir el login, el refresh ni el logout. */
    private static BearerTokenResolver ignoringPublicEndpoints(RequestMatcher publicRequests) {
        DefaultBearerTokenResolver delegate = new DefaultBearerTokenResolver();
        return (HttpServletRequest request) -> publicRequests.matches(request) ? null : delegate.resolve(request);
    }
}
