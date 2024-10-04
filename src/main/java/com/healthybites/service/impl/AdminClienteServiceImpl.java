
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

import com.healthybites.model.entity.Meta;
import com.healthybites.repository.MetaRepository;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminClienteServiceImpl implements AdminClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    private final MetaRepository metaRepository;

    @Transactional(readOnly = true)
    @Override
    public List<ClienteDTO> getAll() {
        List<Cliente> clientes = clienteRepository.findAll();
        return clientes.stream()
                .map(clienteMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ClienteDTO> paginate(Pageable pageable) {
        Page<Cliente> clientes = clienteRepository.findAll(pageable);
        return clientes.map(clienteMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public ClienteDTO findById(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID: " + id + " no fue encontrado"));
        return clienteMapper.toDTO(cliente);
    }

    @Transactional
    @Override
    public ClienteDTO create(ClienteDTO clienteDTO) {
        clienteRepository.findByNombreAndApellido(clienteDTO.getNombre(), clienteDTO.getApellido())
                .ifPresent(existeCliente -> {
                    throw new BadRequestException("Cliente ya existe");
                });
        Cliente cliente = clienteMapper.toEntity(clienteDTO);

        cliente = clienteRepository.save(cliente);
        return clienteMapper.toDTO(cliente);
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
        return clienteMapper.toDTO(clienteFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con ID: " + id + " no fue encontrado"));
        clienteRepository.delete(cliente);
    }


    @Transactional
    public ClienteDTO updateProfileAndMeta(Integer clienteId, ClienteDTO clienteDTO) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        cliente.setNombre(clienteDTO.getNombre());
        cliente.setApellido(clienteDTO.getApellido());
        cliente.setCorreo(clienteDTO.getCorreo());
        cliente.setSexo(clienteDTO.getSexo());
        cliente.setEdad(clienteDTO.getEdad());
        cliente.setAltura(clienteDTO.getAltura());
        cliente.setPeso(clienteDTO.getPeso());

        if (clienteDTO.getMeta() != null) {
            Meta meta = metaRepository.findById(clienteDTO.getMeta().getId())
                    .orElseThrow(() -> new RuntimeException("Meta no encontrada"));
            meta.setNombre(clienteDTO.getMeta().getNombre());
            meta.setDescripcion(clienteDTO.getMeta().getDescripcion());
            meta.setPesoObjetivo(clienteDTO.getMeta().getPesoObjetivo());
            cliente.setMeta(meta);
        }

        Cliente updatedCliente = clienteRepository.save(cliente);
        return clienteMapper.toDTO(updatedCliente);
    }
}
