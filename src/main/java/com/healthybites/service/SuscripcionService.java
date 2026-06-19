package com.healthybites.service;

import com.healthybites.dto.SuscripcionCreateUpdateDTO;
import com.healthybites.dto.SuscripcionDetailsDTO;

import java.util.List;

public interface SuscripcionService {
    List<SuscripcionDetailsDTO> getAll();
    SuscripcionDetailsDTO findById(Integer id);
    SuscripcionDetailsDTO create(SuscripcionCreateUpdateDTO suscripcionCreateUpdateDTO);
    void delete(Integer id);

    SuscripcionDetailsDTO confirmSuscripcion(Integer id);
    SuscripcionDetailsDTO getSuscripcionId(Integer id);
}
