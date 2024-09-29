package com.healthybites.service.impl;

import com.healthybites.model.entity.Pago;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.repository.PagoRepository;
import com.healthybites.repository.SuscripcionRepository;
import com.healthybites.service.AdminPagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminPagoServiceImpl implements AdminPagoService {

    private final PagoRepository pagoRepository;
    private final SuscripcionRepository suscripcionRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Pago> getAll() {
        return pagoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Pago> paginate(Pageable pageable) {
        return pagoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Pago findById(Integer id) {
        return pagoRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Pago no encontrado"));
    }

    @Transactional
    @Override
    public Pago create(Pago pago) {
        Suscripcion suscripcion = suscripcionRepository.findById(pago.getSuscripcion().getId()).
                orElseThrow(() -> new RuntimeException("Suscripcion no encontrada por ID: " + pago.getSuscripcion().getId()));

        pago.setSuscripcion(suscripcion);
        return pagoRepository.save(pago);
    }

    @Transactional
    @Override
    public Pago update(Integer id, Pago updatePago) {
        Pago pagoFromDB = findById(id);

        Suscripcion suscripcion = suscripcionRepository.findById(updatePago.getSuscripcion().getId()).
                orElseThrow(() -> new RuntimeException("Suscripcion no encontrada por ID: " + updatePago.getSuscripcion().getId()));

        pagoFromDB.setEstadoPago(updatePago.getEstadoPago());
        pagoFromDB.setSuscripcion(suscripcion);
        return pagoRepository.save(pagoFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Pago pago = pagoRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Pago no encontrado por ID: " + id));
        pagoRepository.delete(pago);
    }
}
