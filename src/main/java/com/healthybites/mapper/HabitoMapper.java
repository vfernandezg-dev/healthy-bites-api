package com.healthybites.mapper;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import com.healthybites.model.entity.Habito;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class HabitoMapper {

    private final ModelMapper modelMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public HabitoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public HabitoDTO toDTO(Habito habito) {
        HabitoDTO habitoDetailsDTO = modelMapper.map(habito, HabitoDTO.class);
        habitoDetailsDTO.setNombreCliente(habito.getCliente().getNombre() + " " + habito.getCliente().getApellido());
        return habitoDetailsDTO;
    }

    public Habito toEntity(HabitoCreateDTO habitoCreateDTO) {
        Habito habito = modelMapper.map(habitoCreateDTO, Habito.class);
        LocalDate fecha = LocalDate.parse(habitoCreateDTO.getFechaRegistro(), formatter);
        LocalTime horaActual = LocalTime.now();
        habito.setFechaRegistro(LocalDateTime.of(fecha, horaActual));

        return habito;
    }

    public void updateHabitoFromDto(HabitoCreateDTO habitoCreateDTO, Habito habito) {
        // Actualizar campos directamente usando ModelMapper
        modelMapper.map(habitoCreateDTO, habito);

        // Si el DTO tiene una fecha, conviértela y actualiza el campo de fecha en Habito
        if (habitoCreateDTO.getFechaRegistro() != null) {
            LocalDate fecha = LocalDate.parse(habitoCreateDTO.getFechaRegistro(), formatter);
            LocalTime horaActual = LocalTime.now();
            habito.setFechaRegistro(LocalDateTime.of(fecha, horaActual));
        }
    }


    public HabitoCreateDTO toCreateUpdateDTO(Habito habito) {
        return modelMapper.map(habito, HabitoCreateDTO.class);
    }
}
