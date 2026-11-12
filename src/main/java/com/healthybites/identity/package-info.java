/**
 * Identidad: cuentas, sesiones (access token JWT de vida corta + refresh token rotatorio en una cookie httpOnly) y
 * restablecimiento de contraseña.
 *
 * <p>API para el resto de módulos: {@link com.healthybites.identity.UserAccounts}, {@link
 * com.healthybites.identity.UserAccount} y su {@code Role} (en shared.security). Los paquetes {@code domain},
 * {@code app} y {@code web} son internos.
 */
@ApplicationModule(displayName = "Identity")
@NullMarked
package com.healthybites.identity;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
