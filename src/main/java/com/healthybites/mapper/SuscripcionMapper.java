package com.healthybites.mapper;

import com.healthybites.dto.SuscripcionCreateUpdateDTO;
import com.healthybites.dto.SuscripcionDetailsDTO;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.entity.Usuario;
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

        suscripcionDetailsDTO.setPrecio(suscripcion.getPrecio());


        return suscripcionDetailsDTO;
    }

    public Suscripcion toEntity(SuscripcionCreateUpdateDTO suscripcionCreateUpdateDTO) {
        Suscripcion suscripcion = modelMapper.map(suscripcionCreateUpdateDTO, Suscripcion.class);

        Usuario usuario = new Usuario();
        usuario.setId(suscripcionCreateUpdateDTO.getUsuarioId());
        suscripcion.setUsuario(usuario);

        return suscripcion;

    }

}
