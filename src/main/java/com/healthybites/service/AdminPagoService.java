package com.healthybites.service;

import com.healthybites.model.entity.Pago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminPagoService {
    List<Pago> getAll();
    Page<Pago> paginate(Pageable pageable);
    Pago findById(Integer id);
    Pago create(Pago pago);
    Pago update(Integer id, Pago updatePago);
    void delete(Integer id);
}
