package com.healthybites.service.impl;

import com.healthybites.dto.NutricionistaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.NutricionistaMapper;
import com.healthybites.model.entity.Nutricionista;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.service.AdminNutricionistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class AdminNutricionistaServiceImpl implements AdminNutricionistaService {

    @Autowired
    private final NutricionistaRepository nutricionistaRepository;
    @Autowired
    private final NutricionistaMapper nutricionistaMapper;

    @Transactional(readOnly = true)
    @Override
    public List<NutricionistaDTO> getAll() {
        List<Nutricionista> nutricionistas = nutricionistaRepository.findAll();
        return nutricionistas.stream()
                .map(nutricionistaMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<NutricionistaDTO> paginate(Pageable pageable) {
        Page<Nutricionista> nutricionistas = nutricionistaRepository.findAll(pageable);
        return nutricionistas.map(nutricionistaMapper::toDto);
    }

    @Transactional(readOnly = true)
    @Override
    public NutricionistaDTO findById(Integer id) {
        Nutricionista nutricionista = nutricionistaRepository.findById(id).orElseThrow(()-> new ResourceNotFoundException("El author no fue encontrado"));
        return nutricionistaMapper.toDto(nutricionista);
    }

    @Transactional
    @Override
    public NutricionistaDTO create(NutricionistaDTO nutricionistaDTO) {
        nutricionistaRepository.findByNombreAndApellido(nutricionistaDTO.getNombre(), nutricionistaDTO.getApellido())
                .ifPresent(existingNutricionista -> {
                    throw new BadRequestException("El nutricionista ya existe");
                });

        Nutricionista nutricionista = nutricionistaMapper.toEntity(nutricionistaDTO);
        nutricionistaRepository.save(nutricionista);
        return nutricionistaMapper.toDto(nutricionista);
    }

    @Transactional
    @Override
    public NutricionistaDTO update(Integer id, NutricionistaDTO updateNutricionistaDTO) {
       Nutricionista nutricionistaFromDb = nutricionistaRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("El nutricionista no existe"));

       nutricionistaRepository.findByNombreAndApellido(updateNutricionistaDTO.getNombre(), updateNutricionistaDTO.getApellido())
               .filter(existingNutri -> !existingNutri.getId().equals(id))
               .ifPresent(existingNutricionista -> {
                   throw new BadRequestException("El nutricionista ya existe");
               });

       nutricionistaFromDb.setNombre(updateNutricionistaDTO.getNombre());
       nutricionistaFromDb.setApellido(updateNutricionistaDTO.getApellido());

       nutricionistaFromDb = nutricionistaRepository.save(nutricionistaFromDb);
       return nutricionistaMapper.toDto(nutricionistaFromDb);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Nutricionista nutricionista = nutricionistaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("El nutricionista no existe"));
        nutricionistaRepository.delete(nutricionista);
    }
}
