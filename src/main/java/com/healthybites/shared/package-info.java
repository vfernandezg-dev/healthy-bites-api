/**
 * Núcleo compartido: configuración transversal, entidad base, errores RFC 9457, seguridad (JWT, CORS, usuario
 * actual, rutas públicas), correo y limitación de peticiones.
 *
 * <p>Es un módulo abierto: el resto de módulos usa directamente cualquiera de sus tipos. No depende de ningún otro
 * módulo.
 */
@ApplicationModule(displayName = "Shared", type = ApplicationModule.Type.OPEN)
@NullMarked
package com.healthybites.shared;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
