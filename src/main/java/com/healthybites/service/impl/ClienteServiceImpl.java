package com.healthybites.service.impl;

import com.healthybites.dto.ClienteMetaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Meta;
import com.healthybites.model.entity.Usuario;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.MetaRepository;
import com.healthybites.repository.UsuarioRepository;
import com.healthybites.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final MetaRepository metaRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    @Override
    public ClienteMetaDTO actualizarClienteConMeta(Integer clienteId, ClienteMetaDTO dto) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente con id " + clienteId + " no encontrado"));

        // Nombre + apellido deben seguir siendo unicos (excluyendo al propio cliente)
        clienteRepository.findByNombreAndApellido(dto.getNombre(), dto.getApellido())
                .filter(otro -> !otro.getId().equals(clienteId))
                .ifPresent(otro -> {
                    throw new BadRequestException("Ya existe un cliente con el mismo nombre y apellido");
                });

        LocalDateTime ahora = LocalDateTime.now();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setSexo(dto.getSexo());
        cliente.setEdad(dto.getEdad());
        cliente.setAltura(dto.getAltura());
        cliente.setPeso(dto.getPeso());
        cliente.setFechaActualizacion(ahora);

        // El correo vive en Usuario (los clientes creados desde /admin/cliente no tienen usuario)
        Usuario usuario = cliente.getUsuario();
        if (dto.getCorreo() != null && usuario != null && !dto.getCorreo().equalsIgnoreCase(usuario.getCorreo())) {
            if (usuarioRepository.existsByCorreo(dto.getCorreo())) {
                throw new BadRequestException("El correo ya está registrado");
            }
            usuario.setCorreo(dto.getCorreo());
        }

        Meta meta = null;
        if (dto.getMetaID() != null) {
            meta = metaRepository.findByIdAndClienteId(dto.getMetaID(), clienteId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Meta con id " + dto.getMetaID() + " no encontrada para el cliente " + clienteId));
            meta.setNombre(dto.getMetaNombre());
            meta.setDescripcion(dto.getMetaDescripcion());
            meta.setPesoObjetivo(dto.getPesoObjetivo());
            meta.setFechaActualizacion(ahora);
            metaRepository.save(meta);
        }

        clienteRepository.save(cliente);

        ClienteMetaDTO respuesta = new ClienteMetaDTO();
        respuesta.setNombre(cliente.getNombre());
        respuesta.setApellido(cliente.getApellido());
        respuesta.setCorreo(usuario != null ? usuario.getCorreo() : null);
        respuesta.setSexo(cliente.getSexo());
        respuesta.setEdad(cliente.getEdad());
        respuesta.setAltura(cliente.getAltura());
        respuesta.setPeso(cliente.getPeso());
        if (meta != null) {
            respuesta.setMetaID(meta.getId());
            respuesta.setMetaNombre(meta.getNombre());
            respuesta.setMetaDescripcion(meta.getDescripcion());
            respuesta.setPesoObjetivo(meta.getPesoObjetivo());
        }
        return respuesta;
    }
}
