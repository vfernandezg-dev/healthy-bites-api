
package com.healthybites.mapper;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.model.entity.Cliente;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    private final ModelMapper modelMapper;
    private final MetaMapper metaMapper;

    public ClienteMapper(ModelMapper modelMapper, MetaMapper metaMapper) {
        this.modelMapper = modelMapper;
        this.metaMapper = metaMapper;
    }

    public ClienteDTO toDTO(Cliente cliente) {
        ClienteDTO clienteDTO = modelMapper.map(cliente, ClienteDTO.class);
        if (cliente.getMeta() != null) {
            clienteDTO.setMeta(metaMapper.toMetaDTO(cliente.getMeta()));
        }
        return clienteDTO;
    }

    public Cliente toEntity(ClienteDTO clienteDTO) {
        Cliente cliente = modelMapper.map(clienteDTO, Cliente.class);
        if (clienteDTO.getMeta() != null) {
            cliente.setMeta(metaMapper.toEntity(clienteDTO.getMeta()));
        }
        return cliente;
    }
}
