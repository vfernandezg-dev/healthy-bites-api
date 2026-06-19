package com.healthybites.service.impl;

import com.healthybites.dto.RachaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.RachaMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Racha;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.RachaRepository;
import com.healthybites.service.AdminRachaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminRachaServiceImpl implements AdminRachaService {
    @Autowired
    private final RachaRepository rachaRepository;
    @Autowired
    private final ClienteRepository clienteRepository;
    @Autowired
    private final RachaMapper rachaMapper; // Mapper para Racha

    @Transactional(readOnly = true)
    @Override
    public List<RachaDTO> getAll() {
        List<Racha> rachas = rachaRepository.findAll();
        return rachas.stream()
                .map(rachaMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<RachaDTO> paginate(Pageable pageable) {
        Page<Racha> rachas = rachaRepository.findAll(pageable);
        return rachas.map(rachaMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public RachaDTO findById(Integer id) {
        Racha racha = rachaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Racha no encontrada: " + id));
        return rachaMapper.toDto(racha);
    }

    @Transactional
    @Override
    public RachaDTO create(RachaDTO rachaDTO) {
        Racha racha = rachaMapper.toEntity(rachaDTO);

        rachaRepository.save(racha);
        return rachaMapper.toDto(racha);
    }

    @Transactional
    @Override
    public RachaDTO update(Integer id, RachaDTO updateRachaDTO) {
        Racha rachaFromDb = rachaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Racha no encontrada: " + id));

        Cliente cliente = clienteRepository.findById(updateRachaDTO.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + updateRachaDTO.getId()));

        rachaFromDb.setDiasConsecutivos(updateRachaDTO.getDiasConsecutivos());
        rachaFromDb.setUltimaFechaRegistro(updateRachaDTO.getUltimaFechaRegistro());
        rachaFromDb.setCliente(cliente);
        return rachaMapper.toDto(rachaRepository.save(rachaFromDb));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Racha racha = rachaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Racha no encontrada: " + id));
        rachaRepository.delete(racha);
    }

    @Override
    @Transactional
    public RachaDTO asignarRachaACliente(Integer rachaId, Integer clienteId) {
        // Busca la racha y el cliente por su ID
        Racha racha = rachaRepository.findById(rachaId)
                .orElseThrow(() -> new ResourceNotFoundException("Racha no encontrada: " + rachaId));

        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado: " + clienteId));

        // Asigna la racha al cliente (puedes hacer ajustes según tu lógica)
        racha.setCliente(cliente);

        // Guarda la racha actualizada
        rachaRepository.save(racha);

        // Retorna el DTO de la racha
        return rachaMapper.toDto(racha);
    }



}



