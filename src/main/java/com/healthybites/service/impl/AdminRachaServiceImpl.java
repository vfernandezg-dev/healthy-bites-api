package com.healthybites.service.impl;


import com.healthybites.model.entity.Racha;
import com.healthybites.repository.RachaRepository;
import com.healthybites.service.AdminRachaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminRachaServiceImpl implements AdminRachaService {

    private final RachaRepository rachaRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Racha> getAll() {
        return rachaRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Racha> paginate(Pageable pageable) {
        return rachaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Racha findById(Integer id) {
        return rachaRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Racha no encontrada"));
    }

    @Transactional
    @Override
    public Racha create(Racha racha) {
        return rachaRepository.save(racha);
    }

    @Transactional
    @Override
    public Racha update(Integer id, Racha updateRacha) {
        Racha rachaFromDB = findById(id);
        rachaFromDB.setDiasConsecutivos(updateRacha.getDiasConsecutivos());
        rachaFromDB.setUltimaFechaRegistro(updateRacha.getUltimaFechaRegistro());
        return rachaRepository.save(rachaFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Racha racha = findById(id);
        rachaRepository.delete(racha);
    }
}
