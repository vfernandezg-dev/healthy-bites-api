package com.healthybites.service;

import com.healthybites.dto.RachaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// Una racha es el vinculo cliente <-> recompensa (PK compuesta), por eso se identifica con ambos ids.
public interface AdminRachaService {
    List<RachaDTO> getAll();
    Page<RachaDTO> paginate(Pageable pageable);
    RachaDTO findById(Integer clienteId, Integer recompensaId);
    RachaDTO create(RachaDTO rachaDTO);
    RachaDTO update(Integer clienteId, Integer recompensaId, RachaDTO updateRachaDTO);
    void delete(Integer clienteId, Integer recompensaId);
    RachaDTO asignarRachaACliente(Integer recompensaId, Integer clienteId);
}
