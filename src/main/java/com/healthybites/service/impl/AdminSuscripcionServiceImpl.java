package com.healthybites.service.impl;

import com.healthybites.model.entity.Cliente;
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
    private final ClienteRepository clienteRepository;

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
        Cliente cliente = clienteRepository.findById(suscripcion.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + suscripcion.getCliente().getId()));

        suscripcion.setCliente(cliente);
        return suscripcionRepository.save(suscripcion);
    }

    @Transactional
    @Override
    public Suscripcion update(Integer id, Suscripcion updateSuscripcion) {
        Suscripcion suscripcionFromDB = findById(id);

        Cliente cliente = clienteRepository.findById(updateSuscripcion.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + updateSuscripcion.getCliente().getId()));

        suscripcionFromDB.setTipoSuscripcion(updateSuscripcion.getTipoSuscripcion());
        suscripcionFromDB.setPrecio(updateSuscripcion.getPrecio());
        suscripcionFromDB.setFechaInicio(updateSuscripcion.getFechaInicio());
        suscripcionFromDB.setFechaFin(updateSuscripcion.getFechaFin());
        suscripcionFromDB.setCliente(cliente);
        return suscripcionRepository.save(suscripcionFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Suscripcion suscripcion = suscripcionRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Suscripcion no encontrada por ID: " + id));
        suscripcionRepository.delete(suscripcion);
    }
}
