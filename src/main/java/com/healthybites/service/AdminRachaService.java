package com.healthybites.service;

import com.healthybites.model.entity.Racha;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminRachaService {
    List<Racha> getAll();
    Page<Racha> paginate(Pageable pageable);
    Racha findById(Integer id);
    Racha create(Racha racha);
    Racha update(Integer id, Racha updateRacha);
    void delete(Integer id);
}
