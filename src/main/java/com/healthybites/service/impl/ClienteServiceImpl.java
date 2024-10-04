package com.healthybites.service.impl;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.dto.ClienteMetaDTO;
import com.healthybites.dto.LoginRequest;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Meta;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.MetaRepository;
import com.healthybites.service.ClienteService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;


@RequiredArgsConstructor
@Service

public class ClienteServiceImpl implements ClienteService {
    private final ClienteRepository clienteRepository;
    private final MetaRepository metaRepository;

    @Transactional
    @Override
    public Cliente registerCliente(Cliente cliente) {
        if(clienteRepository.existsByCorreo(cliente.getCorreo())){
            throw new RuntimeException("El correo ya está registrado");
        }
        cliente.setCreatedAt(LocalDateTime.now());
        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente loginCliente(LoginRequest loginRequest) {
        Optional<Cliente> clienteOpt = clienteRepository.findByCorreo(loginRequest.getCorreo());

        // Verificar si el cliente existe y si la contraseña es correcta
        if (clienteOpt.isPresent() && clienteOpt.get().getContrasena().equals(loginRequest.getContrasena())) {
            return clienteOpt.get();  // Retorna el cliente si las credenciales son correctas
        } else {
            throw new RuntimeException("Credenciales inválidas");
        }

    }

    @Override
    public ClienteDTO actualizarClienteConMeta(Integer clienteId, ClienteMetaDTO clienteMetaDTO) {
        // Obtener cliente por ID
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Actualizar datos del cliente
        cliente.setNombre(clienteMetaDTO.getNombre());
        cliente.setApellido(clienteMetaDTO.getApellido());
        cliente.setCorreo(clienteMetaDTO.getCorreo());
        cliente.setSexo(clienteMetaDTO.getSexo());
        cliente.setEdad(clienteMetaDTO.getEdad());
        cliente.setAltura(clienteMetaDTO.getAltura());
        cliente.setPeso(clienteMetaDTO.getPeso());

        // Actualizar meta
        Meta meta = metaRepository.findById(clienteMetaDTO.getMetaID())
                .orElseThrow(() -> new IllegalArgumentException("Meta no encontrada"));
        meta.setNombre(clienteMetaDTO.getMetaNombre());
        meta.setDescripcion(clienteMetaDTO.getMetaDescripcion());
        meta.setPesoObjetivo(clienteMetaDTO.getPesoObjetivo());

        // Guardar los cambios
        cliente.setMeta(meta);
        clienteRepository.save(cliente);

        // Retornar DTO actualizado
        return new ClienteDTO(cliente);
    }

    @Override
    public ClienteDTO actualizarCliente(Integer clienteId, ClienteDTO clienteDTO) {
        return null;
    }
}

