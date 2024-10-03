package com.healthybites.service.impl;

import com.healthybites.model.entity.Seguimiento;
import com.healthybites.repository.SeguimientoRepository;
import com.healthybites.service.AdminSeguimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminSeguimientoServiceImpl implements AdminSeguimientoService {

    private final SeguimientoRepository seguimientoRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Seguimiento> getAll() {
        return seguimientoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Seguimiento> paginate(Pageable pageable) {
        return seguimientoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Seguimiento findById(Integer id) {
        return seguimientoRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Seguimiento no encontrado"));
    }

    @Transactional
    @Override
    public Seguimiento create(Seguimiento seguimiento) {
        return seguimientoRepository.save(seguimiento);
    }

    @Transactional
    @Override
    public Seguimiento update(Integer id, Seguimiento updateSeguimiento) {
        Seguimiento seguimientoFromDB = findById(id);
        seguimientoFromDB.setFecha(updateSeguimiento.getFecha());
        seguimientoFromDB.setPesoDelDia(updateSeguimiento.getPesoDelDia());
        seguimientoFromDB.setObservaciones(updateSeguimiento.getObservaciones());
        return seguimientoRepository.save(seguimientoFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Seguimiento seguimiento = findById(id);
        seguimientoRepository.delete(seguimiento);
    }
}
