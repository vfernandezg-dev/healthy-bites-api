package com.healthybites.mapper;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;
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

    public SeguimientoDTO toDTO(Seguimiento seguimiento) {
        SeguimientoDTO seguimientoDetailsDTO = modelMapper.map(seguimiento, SeguimientoDTO.class);

        seguimientoDetailsDTO.setNombreMeta(seguimiento.getMeta().getNombre());

        return seguimientoDetailsDTO;
    }

    public Seguimiento toEntity(SeguimientoCreateDTO seguimientoCreateDTO) {
        Seguimiento seguimiento = modelMapper.map(seguimientoCreateDTO, Seguimiento.class);

        // Convertir el String de fecha a LocalDate
        LocalDate fecha = LocalDate.parse(seguimientoCreateDTO.getFecha(), formatter);

        // Obtener la hora actual del sistema
        LocalTime horaActual = LocalTime.now();

        // Combinar la fecha con la hora actual
        seguimiento.setFecha(LocalDateTime.of(fecha, horaActual));

        return seguimiento;
    }

    public SeguimientoCreateDTO toCreateUpdateDTO(Seguimiento seguimiento) {
        return modelMapper.map(seguimiento, SeguimientoCreateDTO.class);
    }
}
