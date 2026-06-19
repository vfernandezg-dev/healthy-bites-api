package com.healthybites.service;

import com.healthybites.dto.RecompensaCreateUpdateDTO;
import com.healthybites.dto.RecompensaDetailsDTO;

import java.util.List;

public interface RecompensaService {
    List<RecompensaDetailsDTO> getAll();
    RecompensaDetailsDTO findById(Integer id);
    RecompensaDetailsDTO create(RecompensaCreateUpdateDTO recompensaCreateUpdateDTO);
    RecompensaDetailsDTO update(Integer id, RecompensaCreateUpdateDTO updateRecompensaDTO);
    void delete(Integer id);
}
