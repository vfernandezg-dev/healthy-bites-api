package com.healthybites.service;

import com.healthybites.model.entity.Habito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AdminHabitoService {
    List<Habito> getAll();
    Page<Habito> paginate(Pageable pageable);
    Habito findById(Integer id);
    Habito create(Habito habito);
    Habito update(Integer id, Habito updateHabito);
    void delete(Integer id);
}
