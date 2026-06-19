package com.healthybites.mapper;

import com.healthybites.dto.SeguimientoCreateUpdateDTO;
import com.healthybites.dto.SeguimientoDetailsDTO;
import com.healthybites.model.entity.Seguimiento;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class SeguimientoMapper {

    private final ModelMapper modelMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public SeguimientoMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
        this.modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
    }

    public SeguimientoDetailsDTO toDTO(Seguimiento seguimiento) {
        SeguimientoDetailsDTO seguimientoDetailsDTO = modelMapper.map(seguimiento, SeguimientoDetailsDTO.class);

        return seguimientoDetailsDTO;
    }

    public Seguimiento toEntity(SeguimientoCreateUpdateDTO seguimientoCreateUpdateDTO) {
        Seguimiento seguimiento = modelMapper.map(seguimientoCreateUpdateDTO, Seguimiento.class);

        // Convertir el String de fecha a LocalDate
        LocalDate fecha = LocalDate.parse(seguimientoCreateUpdateDTO.getFecha(), formatter);

        // Obtener la hora actual del sistema
        LocalTime horaActual = LocalTime.now();

        // Combinar la fecha con la hora actual
        seguimiento.setFecha(LocalDateTime.of(fecha, horaActual));

        return seguimiento;
    }

    public SeguimientoCreateUpdateDTO toCreateUpdateDTO(Seguimiento seguimiento) {
        return modelMapper.map(seguimiento, SeguimientoCreateUpdateDTO.class);
    }
}
