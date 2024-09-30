package com.healthybites.service;

import com.healthybites.model.entity.Meta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminMetaService {
    List<Meta> getAll();
    Page<Meta> paginate(Pageable pageable);
    Meta findById(Integer id);
    Meta create(Meta meta);
    Meta update(Integer id, Meta updateMeta);
    void delete(Integer id);
}
