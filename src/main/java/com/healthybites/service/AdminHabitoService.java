package com.healthybites.service;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminHabitoService {
    List<HabitoDTO> getAll();
    Page<HabitoDTO> paginate(Pageable pageable);
    HabitoDTO findById(Integer id);
    HabitoDTO create(HabitoCreateDTO habitoCreateDTO);
    HabitoDTO update(Integer id, HabitoCreateDTO updateHabitoDTO);
    void delete(Integer id);
    HabitoDTO asignarHabitoACliente(Integer habitoId, Integer clienteId);
}
