package com.healthybites.service.impl;

import com.healthybites.model.entity.Cliente;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.service.ClienteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service

public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;

    @Transactional
    @Override
    public Cliente registerCliente(Cliente cliente) {
        if(clienteRepository.existsByCorreo(cliente.getCorreo())){
            throw new RuntimeException("El correo ya está registrado");
        }
        cliente.setCreatedAt(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }
}


