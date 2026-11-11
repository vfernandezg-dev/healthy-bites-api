package com.healthybites.service;

import com.healthybites.dto.ContactMessageDTO;

public interface ContactService {

    /**
     * Envía al equipo el mensaje del formulario de contacto de la landing page.
     *
     * @param message   datos validados del formulario
     * @param clientKey identificador del cliente (IP) para limitar la frecuencia de envíos
     */
    void sendMessage(ContactMessageDTO message, String clientKey);
}
