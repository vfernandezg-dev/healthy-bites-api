/**
 * Eventos de identity que otros módulos pueden escuchar (por ejemplo, notifications). Solo llevan ids: quien los
 * escucha consulta lo que necesite a través de la API del módulo.
 */
@NamedInterface("events")
@NullMarked
package com.healthybites.identity.events;

import org.jspecify.annotations.NullMarked;
import org.springframework.modulith.NamedInterface;
