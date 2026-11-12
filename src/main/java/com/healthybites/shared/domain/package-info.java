/**
 * Piezas comunes del dominio: entidades base, publicación e importes.
 *
 * <p>{@code shared} es un módulo abierto y Spring Modulith ya permite usar todos sus tipos. Declarar cada subpaquete
 * como interfaz con nombre lo hace explícito para las herramientas que no entienden los módulos abiertos (la
 * validación de Modulith de Spring Boot Tools en el IDE marcaría cada uso como una violación).
 */
@NamedInterface("domain")
@NullMarked
package com.healthybites.shared.domain;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
