package com.healthybites.mapper;

import com.healthybites.dto.SuscripcionCreateUpdateDTO;
import com.healthybites.dto.SuscripcionDetailsDTO;
import com.healthybites.model.entity.Suscripcion;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class SuscripcionMapper {

    private final ModelMapper modelMapper;

    public SuscripcionMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public SuscripcionDetailsDTO toDTO(Suscripcion suscripcion) {
        SuscripcionDetailsDTO suscripcionDetailsDTO = modelMapper.map(suscripcion, SuscripcionDetailsDTO.class);
        suscripcionDetailsDTO.setClienteNombre(suscripcion.getCliente().getNombre() + " " + suscripcion.getCliente().getApellido());
        suscripcionDetailsDTO.setPrecio(suscripcion.getPrecio());

        return suscripcionDetailsDTO;
    }

    public Suscripcion toEntity(SuscripcionCreateUpdateDTO suscripcionCreateUpdateDTO) {
        return modelMapper.map(suscripcionCreateUpdateDTO, Suscripcion.class);
    }

    public SuscripcionCreateUpdateDTO toCreateUpdateDTO(Suscripcion suscripcion) {
        return modelMapper.map(suscripcion, SuscripcionCreateUpdateDTO.class);
    }


}
