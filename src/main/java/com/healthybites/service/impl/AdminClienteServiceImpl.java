package com.healthybites.service.impl;


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

    @Transactional(readOnly = true)
    @Override
    public List<Cliente> getAll() {
        return clienteRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Cliente> paginate(Pageable pageable) {
        return clienteRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Cliente findById(Integer id) {
        return clienteRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
    }

    @Transactional
    @Override
    public Cliente create(Cliente cliente) {
        return clienteRepository.save(cliente);
    }

    @Transactional
    @Override
    public Cliente update(Integer id, Cliente updateCliente) {
        Cliente clienteFromDB = findById(id);
        clienteFromDB.setNombre(updateCliente.getNombre());
        clienteFromDB.setApellido(updateCliente.getApellido());
        clienteFromDB.setSexo(updateCliente.getSexo());
        clienteFromDB.setEdad(updateCliente.getEdad());
        clienteFromDB.setAltura(updateCliente.getAltura());
        clienteFromDB.setPeso(updateCliente.getPeso());
        return clienteRepository.save(clienteFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Cliente cliente = findById(id);
        clienteRepository.delete(cliente);
    }
}
