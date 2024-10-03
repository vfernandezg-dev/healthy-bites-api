package com.healthybites.service;

import com.healthybites.model.entity.Suscripcion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminSuscripcionService {
    List<Suscripcion> getAll();
    Page<Suscripcion> paginate(Pageable pageable);
    Suscripcion findById(Integer id);
    Suscripcion create(Suscripcion suscripcion);
    Suscripcion update(Integer id, Suscripcion updateSuscripcion);
    void delete(Integer id);
}
