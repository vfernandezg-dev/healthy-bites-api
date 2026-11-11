package com.healthybites.service;

import com.healthybites.dto.RecompensaCreateUpdateDTO;
import com.healthybites.dto.RecompensaDetailsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminRecompensaService {
    List<RecompensaDetailsDTO> getAll();
    Page<RecompensaDetailsDTO> paginate(Pageable pageable);
    RecompensaDetailsDTO findById(Integer id);
    RecompensaDetailsDTO create(RecompensaCreateUpdateDTO recompensaDTO);
    RecompensaDetailsDTO update(Integer id, RecompensaCreateUpdateDTO updateRecompensaDTO);
    void delete(Integer id);
    RecompensaDetailsDTO asignarRecompensaACliente(Integer recompensaId, Integer clienteId);
}
