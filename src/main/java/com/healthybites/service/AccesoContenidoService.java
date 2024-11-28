package com.healthybites.service;

import com.healthybites.dto.ContenidoDTO;

import java.util.List;

public interface AccesoContenidoService {
    // Method to add content to a client
    ContenidoDTO addContentToClient(Integer clientId, Integer contentId);

    // Method to remove content from a client
    void removeContentFromClient(Integer clientId, Integer contentId);

    // Method to get all content by client
    List<ContenidoDTO> getAllContentByClient(Integer clientId);

    boolean isClientePremiumOrVip(Integer clientId);
}
