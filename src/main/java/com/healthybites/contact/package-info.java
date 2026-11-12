/**
 * Formulario de contacto de la landing ({@code POST /contact}). El contrato con la landing no cambia: campos en
 * español ({@code nombre}, {@code apellido}, {@code correo}, {@code mensaje}, {@code website}), respuesta síncrona y
 * códigos 204/422/429/503 (con un 503 la landing ofrece {@code mailto:}).
 */
@ApplicationModule(displayName = "Contact")
@NullMarked
package com.healthybites.contact;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
