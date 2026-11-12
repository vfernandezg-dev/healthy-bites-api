/**
 * Perfiles: datos de cada rol (cliente: sexo, fecha de nacimiento, altura y peso; nutricionista: bio), registro
 * público de clientes, alta de nutricionistas por invitación y {@code /me}. La PK de cada perfil es el id del
 * usuario: una persona, un id.
 *
 * <p>API para otros módulos: {@link com.healthybites.profiles.ClientProfiles}.
 */
@ApplicationModule(displayName = "Profiles")
@NullMarked
package com.healthybites.profiles;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
