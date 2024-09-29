package com.healthybites.service;

import com.healthybites.model.entity.Seguimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminSeguimientoService {
    List<Seguimiento> getAll();
    Page<Seguimiento> paginate(Pageable pageable);
    Seguimiento findById(Integer id);
    Seguimiento create(Seguimiento seguimiento);
    Seguimiento update(Integer id, Seguimiento updateSeguimiento);
    void delete(Integer id);
}
