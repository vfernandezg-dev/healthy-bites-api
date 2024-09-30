package com.healthybites.service;

import com.healthybites.model.entity.Recompensa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminRecompensaService {
    List<Recompensa> getAll();
    Page<Recompensa> paginate(Pageable pageable);
    Recompensa findById(Integer id);
    Recompensa create(Recompensa recompensa);
    Recompensa update(Integer id, Recompensa updateRecompensa);
    void delete(Integer id);
}
