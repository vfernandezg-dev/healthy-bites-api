package com.healthybites.service;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.dto.RecompensaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminRecompensaService {
    List<RecompensaDTO> getAll();
    Page<RecompensaDTO> paginate(Pageable pageable);
    RecompensaDTO findById(Integer id);
    RecompensaDTO create(RecompensaDTO recompensaDTO);
    RecompensaDTO update(Integer id, RecompensaDTO updateRecompensaDTO);
    void delete(Integer id);
    RecompensaDTO asignarRecompensaACliente(Integer recompensaId, Integer clienteId);
}
