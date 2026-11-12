package com.healthybites.shared.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * Solo para cuentas CLIENT (el resto recibe 403). Las rutas de ADMIN y NUTRITIONIST se protegen por prefijo
 * ({@code /admin/**}, {@code /nutritionist/**}) en {@link SecurityConfiguration}; las del cliente comparten
 * {@code /me} con los demás roles, así que se marcan con esta anotación.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('CLIENT')")
public @interface ClientOnly {}
