package com.healthybites.service;

import com.healthybites.dto.MetaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminMetaService {
    List<MetaDTO> getAll();
    Page<MetaDTO> paginate(Pageable pageable);
    MetaDTO findById(Integer id);
    MetaDTO create(MetaDTO metaDTO);
    MetaDTO update(Integer id, MetaDTO updateMetaDTO);
    void delete(Integer id);
}
