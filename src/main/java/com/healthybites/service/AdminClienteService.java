package com.healthybites.service;

import com.healthybites.dto.ClienteCreateDTO;
import com.healthybites.dto.ClienteDetailsDTO;

import java.util.List;

public interface AdminClienteService {
    List<ClienteDetailsDTO> getAll();
    ClienteDetailsDTO findById(Integer id);
    ClienteDetailsDTO create(ClienteCreateDTO clienteCreateDTO);
    ClienteDetailsDTO update(Integer id, ClienteCreateDTO updatedClienteCreateDTO);
    void delete(Integer id);
}
