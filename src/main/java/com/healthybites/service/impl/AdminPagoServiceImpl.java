package com.healthybites.service.impl;

import com.healthybites.model.entity.Pago;
import com.healthybites.repository.PagoRepository;
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
        return pagoRepository.save(pago);
    }

    @Transactional
    @Override
    public Pago update(Integer id, Pago updatePago) {
        Pago pagoFromDB = findById(id);
        pagoFromDB.setEstadoPago(updatePago.getEstadoPago());
        return pagoRepository.save(pagoFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Pago pago = findById(id);
        pagoRepository.delete(pago);
    }
}
