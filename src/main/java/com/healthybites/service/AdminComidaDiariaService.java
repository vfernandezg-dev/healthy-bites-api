package com.healthybites.service;

import com.healthybites.model.entity.ComidaDiaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminComidaDiariaService {
    List<ComidaDiaria> getAll();
    Page<ComidaDiaria> paginate(Pageable pageable);
    ComidaDiaria findById(Integer id);
    ComidaDiaria create(ComidaDiaria comidaDiaria);
    ComidaDiaria update(Integer id, ComidaDiaria updateComidaDiaria);
    void delete(Integer id);
}
