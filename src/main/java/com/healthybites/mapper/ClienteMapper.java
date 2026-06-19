package com.healthybites.mapper;

import com.healthybites.dto.ClienteCreateDTO;
import com.healthybites.dto.ClienteDetailsDTO;
import com.healthybites.model.entity.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    private final ModelMapper modelMapper;

    public ClienteMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public ClienteDetailsDTO toDTO(Cliente cliente) {
        ClienteDetailsDTO clienteDetailsDTO = modelMapper.map(cliente, ClienteDetailsDTO.class);
        return clienteDetailsDTO;
    }

    public Cliente toEntity(ClienteCreateDTO clienteCreateDTO) {
        return modelMapper.map(clienteCreateDTO, Cliente.class);
    }

}