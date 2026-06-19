package com.healthybites.service.impl;

import com.healthybites.model.entity.Meta;
import com.healthybites.model.entity.Seguimiento;
import com.healthybites.repository.MetaRepository;
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
    private final MetaRepository metaRepository;

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
        Meta meta = metaRepository.findById(seguimiento.getMeta().getId())
                .orElseThrow(() -> new RuntimeException("Meta no encontrada con id: " + seguimiento.getMeta().getId()));

        seguimiento.setMeta(meta);
        return seguimientoRepository.save(seguimiento);
    }

    @Transactional
    @Override
    public Seguimiento update(Integer id, Seguimiento updateSeguimiento) {
        Seguimiento seguimientoFromDB = findById(id);
        Meta meta = metaRepository.findById(updateSeguimiento.getMeta().getId())
                .orElseThrow(() -> new RuntimeException("Meta no encontrada con id: " + updateSeguimiento.getMeta().getId()));


        seguimientoFromDB.setFecha(updateSeguimiento.getFecha());
        seguimientoFromDB.setPesoDelDia(updateSeguimiento.getPesoDelDia());
        seguimientoFromDB.setObservaciones(updateSeguimiento.getObservaciones());
        seguimientoFromDB.setMeta(meta);
        return seguimientoRepository.save(seguimientoFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Seguimiento seguimiento = findById(id);
        seguimientoRepository.delete(seguimiento);
    }
}
