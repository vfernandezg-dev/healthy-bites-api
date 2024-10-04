package com.healthybites.service;

import com.healthybites.dto.HabitoDTO;
import com.healthybites.dto.RachaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminHabitoService {
    List<HabitoDTO> getAll();
    Page<HabitoDTO> paginate(Pageable pageable);
    HabitoDTO findById(Integer id);
    HabitoDTO create(HabitoDTO habitoDTO);
    HabitoDTO update(Integer id, HabitoDTO updateHabitoDTO);
    void delete(Integer id);
    HabitoDTO asignarHabitoACliente(Integer habitoId, Integer clienteId);
}
