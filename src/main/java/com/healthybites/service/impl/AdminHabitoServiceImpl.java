package com.healthybites.service.impl;

import com.healthybites.dto.HabitoDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.HabitoMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Habito;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.HabitoRepository;
import com.healthybites.service.AdminHabitoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class AdminHabitoServiceImpl implements AdminHabitoService {

    @Autowired
    private final HabitoRepository habitoRepository;
    private final ClienteRepository clienteRepository;

    @Autowired
    private final HabitoMapper habitoMapper;

    @Autowired
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    @Override
    public List<HabitoDTO> getAll() {
        List<Habito> habitos = habitoRepository.findAll();
        return habitos.stream()
                .map(habitoMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<HabitoDTO> paginate(Pageable pageable) {
        Page<Habito> habitos = habitoRepository.findAll(pageable);
        return habitos.map(habitoMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public HabitoDTO findById(Integer id) {
        Habito habito = habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));
        return habitoMapper.toDto(habito);
    }

    @Transactional
    @Override
    public HabitoDTO create(HabitoDTO habitoDTO) {
        Habito habito = habitoMapper.toEntity(habitoDTO);
        habito = habitoRepository.save(habito);
        return habitoMapper.toDto(habito);
    }

    @Transactional
    @Override
    public HabitoDTO update(Integer id, HabitoDTO updateHabitoDTO) {
        Habito habitoFromDB = habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));

        habitoFromDB.setNombre(updateHabitoDTO.getNombre());
        habitoFromDB.setHidratacion(updateHabitoDTO.getHidratacion());
        habitoFromDB.setAlimentacion(updateHabitoDTO.getAlimentacion());
        habitoFromDB.setEjercicio(updateHabitoDTO.getEjercicio());
        habitoFromDB.setCalidadDeSueno(updateHabitoDTO.getCalidadDeSueno());

        habitoFromDB = habitoRepository.save(habitoFromDB);
        return habitoMapper.toDto(habitoFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Habito habito = habitoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + id));
        habitoRepository.delete(habito);
    }

    @Transactional
    @Override
    public HabitoDTO asignarHabitoACliente(Integer habitoId, Integer clienteId){
        Habito habito = habitoRepository.findById(habitoId)
                .orElseThrow(() -> new ResourceNotFoundException("Habito no encontrado: " + habitoId));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));

        habito.setCliente(cliente);
        return habitoMapper.toDto(habito);
    }
}

