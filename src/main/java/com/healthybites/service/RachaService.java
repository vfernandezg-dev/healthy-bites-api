package com.healthybites.service;

import com.healthybites.dto.RecompensaDetailsDTO;

import java.util.List;

public interface RachaService {

    RecompensaDetailsDTO addRecompensaToClient(Integer clientId, Integer recompensaId);

    void removeRecompensaFromClient(Integer clientId, Integer recompensaId);

    List<RecompensaDetailsDTO> getAllRecompensasByClient(Integer clientId);

}
