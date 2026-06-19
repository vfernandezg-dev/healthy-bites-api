package com.healthybites.mapper;

import com.healthybites.dto.RecompensaCreateUpdateDTO;
import com.healthybites.dto.RecompensaDetailsDTO;
import com.healthybites.model.entity.Recompensa;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

@Component
public class RecompensaMapper {

    private final ModelMapper modelMapper;

    public RecompensaMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public RecompensaDetailsDTO toDTO(Recompensa recompensa) {
        RecompensaDetailsDTO recompensaDetailsDTO = modelMapper.map(recompensa, RecompensaDetailsDTO.class);
        recompensaDetailsDTO.setNutricionistaNombre(recompensa.getNutricionista().getNombre() + " " + recompensa.getNutricionista().getApellido());

        // Verificar si 'contenido' no es null antes de obtener el título
        if (recompensa.getContenido() != null) {
            recompensaDetailsDTO.setContenidoTitulo(recompensa.getContenido().getTitulo());
        }

        // Verificar si 'planAlimenticio' no es null antes de obtener el título
        if (recompensa.getPlanAlimenticio() != null) {
            recompensaDetailsDTO.setPlanAlimenticioTitulo(recompensa.getPlanAlimenticio().getPlanObjetivo());
        }

        return recompensaDetailsDTO;
    }


    public Recompensa toEntity(RecompensaCreateUpdateDTO recompensaCreateUpdateDTO) {
        return modelMapper.map(recompensaCreateUpdateDTO, Recompensa.class);
    }

    public void updateFromDTO(RecompensaCreateUpdateDTO recompensaCreateUpdateDTO, Recompensa recompensa) {
        modelMapper.map(recompensaCreateUpdateDTO, recompensa);
    }
}
