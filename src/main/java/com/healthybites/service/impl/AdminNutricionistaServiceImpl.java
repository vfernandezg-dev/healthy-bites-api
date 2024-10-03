package com.healthybites.service.impl;

import com.healthybites.model.entity.Nutricionista;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.service.AdminNutricionistaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminNutricionistaServiceImpl implements AdminNutricionistaService {

    private final NutricionistaRepository nutricionistaRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Nutricionista> getAll() {
        return nutricionistaRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Nutricionista> paginate(Pageable pageable) {
        return nutricionistaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Nutricionista findById(Integer id) {
        return nutricionistaRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Nutricionista no encontrado"));
    }

    @Transactional
    @Override
    public Nutricionista create(Nutricionista nutricionista) {
        return nutricionistaRepository.save(nutricionista);
    }

    @Transactional
    @Override
    public Nutricionista update(Integer id, Nutricionista updateNutricionista) {
        Nutricionista nutricionistaFromDB = findById(id);
        nutricionistaFromDB.setNombre(updateNutricionista.getNombre());
        nutricionistaFromDB.setApellido(updateNutricionista.getApellido());
        return nutricionistaRepository.save(nutricionistaFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Nutricionista nutricionista = findById(id);
        nutricionistaRepository.delete(nutricionista);
    }
}
