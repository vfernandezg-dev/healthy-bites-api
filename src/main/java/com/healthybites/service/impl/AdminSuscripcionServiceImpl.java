package com.healthybites.service.impl;

import com.healthybites.model.entity.Suscripcion;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.SuscripcionRepository;
import com.healthybites.service.AdminSuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminSuscripcionServiceImpl implements AdminSuscripcionService {

    private final SuscripcionRepository suscripcionRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Suscripcion> getAll() {
        return suscripcionRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Suscripcion> paginate(Pageable pageable) {
        return suscripcionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Suscripcion findById(Integer id) {
        return suscripcionRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Suscripcion no encontrada"));
    }

    @Transactional
    @Override
    public Suscripcion create(Suscripcion suscripcion) {
        return suscripcionRepository.save(suscripcion);
    }

    @Transactional
    @Override
    public Suscripcion update(Integer id, Suscripcion updateSuscripcion) {
        Suscripcion suscripcionFromDB = findById(id);
        suscripcionFromDB.setTipoSuscripcion(updateSuscripcion.getTipoSuscripcion());
        suscripcionFromDB.setPrecio(updateSuscripcion.getPrecio());
        suscripcionFromDB.setFechaInicio(updateSuscripcion.getFechaInicio());
        suscripcionFromDB.setFechaFin(updateSuscripcion.getFechaFin());
        return suscripcionRepository.save(suscripcionFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Suscripcion suscripcion = findById(id);
        suscripcionRepository.delete(suscripcion);
    }
}
