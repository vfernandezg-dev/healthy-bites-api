/**
 * Errores RFC 9457, paginación, cabecera Location y rangos de fechas.
 *
 * <p>{@code shared} es un módulo abierto y Spring Modulith ya permite usar todos sus tipos. Declarar cada subpaquete
 * como interfaz con nombre lo hace explícito para las herramientas que no entienden los módulos abiertos (la
 * validación de Modulith de Spring Boot Tools en el IDE marcaría cada uso como una violación).
 */
@NamedInterface("web")
@NullMarked
package com.healthybites.shared.web;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
