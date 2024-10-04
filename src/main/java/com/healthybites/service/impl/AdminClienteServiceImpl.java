package com.healthybites.service.impl;


import com.healthybites.dto.ClienteDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ClienteMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.service.AdminClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminClienteServiceImpl implements AdminClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    @Override
    public List<ClienteDTO> getAll() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(clienteMapper::ToDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ClienteDTO> paginate(Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.findAll(pageable);
        return clientes.map(clienteMapper::ToDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public ClienteDTO findById(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID: " + id + " no fue encontrado"));
        return clienteMapper.ToDTO(cliente);
    }

    @Transactional
    @Override
    public ClienteDTO create(ClienteDTO clienteDTO) {
        clienteRepository.findByNombreAndApellido(clienteDTO.getNombre(), clienteDTO.getApellido())
                .ifPresent(existeCliente -> {
                    throw new BadRequestException("Cliente ya existe");
                });
        Cliente cliente = clienteMapper.ToEntity(clienteDTO);

        cliente = clienteRepository.save(cliente);
        return clienteMapper.ToDTO(cliente);
    }

    @Transactional
    @Override
    public ClienteDTO update(Integer id, ClienteDTO updateClienteDTO) {
        Cliente clienteFromDB = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID: " + id + " no fue encontrado"));

        clienteRepository.findByNombreAndApellido(updateClienteDTO.getNombre(), updateClienteDTO.getApellido())
                .filter(existeCliente -> !existeCliente.getId().equals(id))
                .ifPresent(existeCliente -> {
                    throw new BadRequestException("Cliente ya existe");
                });

        clienteFromDB.setNombre(updateClienteDTO.getNombre());
        clienteFromDB.setApellido(updateClienteDTO.getApellido());
        clienteFromDB.setSexo(updateClienteDTO.getSexo());
        clienteFromDB.setEdad(updateClienteDTO.getEdad());
        clienteFromDB.setAltura(updateClienteDTO.getAltura());
        clienteFromDB.setPeso(updateClienteDTO.getPeso());

        clienteFromDB = clienteRepository.save(clienteFromDB);
        return clienteMapper.ToDTO(clienteFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID: " + id + " no fue encontrado"));
        clienteRepository.delete(cliente);
    }
}
