/**
 * Avisos por correo a partir de los eventos de los demás módulos: bienvenida al crear una cuenta, justificante al
 * activarse una suscripción, aviso cuando termina, enhorabuena al ganar una recompensa y solicitud para entrar en un
 * grupo privado (al creador).
 *
 * <p>Los listeners son {@code @ApplicationModuleListener}: se ejecutan después del commit, en otro hilo y con el
 * evento guardado en {@code event_publication}. Si el SMTP falla, el evento queda como fallido y
 * {@code FailedEventRetryJob} lo reintenta; ningún aviso se pierde. Las cuentas desactivadas no reciben avisos.
 *
 * <p>No tiene API pública ni endpoints: solo escucha.
 */
@ApplicationModule(displayName = "Notifications")
@NullMarked
package com.healthybites.notifications;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.ApplicationModule;
