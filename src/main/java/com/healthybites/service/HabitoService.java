package com.healthybites.service;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;

import java.util.List;

public interface HabitoService {
    List<HabitoDTO> getAll(Integer id);
    HabitoDTO registrarHabito(HabitoCreateDTO habitoCreateDTO);
    HabitoDTO actualizarHabito(Integer habitoId, HabitoCreateDTO habitoCreateDTO);
    void delete(Integer habitoId);
}
