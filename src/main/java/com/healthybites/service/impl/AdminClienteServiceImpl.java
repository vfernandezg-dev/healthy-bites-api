package com.healthybites.service.impl;

import com.healthybites.dto.ClienteCreateDTO;
import com.healthybites.dto.ClienteDetailsDTO;
import com.healthybites.mapper.ClienteMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.SuscripcionRepository;
import com.healthybites.service.AdminClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AdminClienteServiceImpl implements AdminClienteService {

    private final ClienteRepository clienteRepository;
    private final SuscripcionRepository suscripcionRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    @Override
    public List<ClienteDetailsDTO> getAll() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(clienteMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ClienteDetailsDTO findById(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El Cliente con id " + id + " no existe"));
        return clienteMapper.toDTO(cliente);
    }

    @Override
    public ClienteDetailsDTO create(ClienteCreateDTO clienteCreateDTO) {
        clienteRepository.findByNombreAndApellido(clienteCreateDTO.getNombre(), clienteCreateDTO.getApellido())
                .ifPresent(cliente -> {
                    throw new RuntimeException("El Cliente ya existe con el mismo nombre y apellido");
                });

        Cliente cliente = clienteMapper.toEntity(clienteCreateDTO);
        cliente.setFechaCreacion(LocalDateTime.now());
        cliente.setFechaActualizacion(LocalDateTime.now());

        Cliente savedCliente = clienteRepository.save(cliente);

        return clienteMapper.toDTO(savedCliente);
    }

    @Override
    public ClienteDetailsDTO update(Integer id, ClienteCreateDTO updatedClienteCreateDTO) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El Cliente con id " + id + " no existe"));

        cliente.setNombre(updatedClienteCreateDTO.getNombre());
        cliente.setApellido(updatedClienteCreateDTO.getApellido());
        cliente.setSexo(updatedClienteCreateDTO.getSexo());
        cliente.setEdad(updatedClienteCreateDTO.getEdad());
        cliente.setAltura(updatedClienteCreateDTO.getAltura());
        cliente.setPeso(updatedClienteCreateDTO.getPeso());
        cliente.setFechaActualizacion(LocalDateTime.now());

        Cliente savedCliente = clienteRepository.save(cliente);

        return clienteMapper.toDTO(savedCliente);
    }

    @Override
    public void delete(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("El Cliente con id " + id + " no existe"));
        clienteRepository.delete(cliente);
    }
}
