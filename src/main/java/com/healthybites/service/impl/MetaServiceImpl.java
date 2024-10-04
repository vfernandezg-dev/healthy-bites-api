package com.healthybites.service.impl;

import com.healthybites.dto.MetaDTO;
import com.healthybites.model.entity.Meta;
import com.healthybites.repository.MetaRepository;
import com.healthybites.service.MetaService;

public class MetaServiceImpl implements MetaService {
    private final MetaRepository metaRepository;

    public MetaServiceImpl(MetaRepository metaRepository) {
        this.metaRepository = metaRepository;
    }

    @Override
    public MetaDTO actualizarMeta(Integer metaId, MetaDTO metaDTO) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new IllegalArgumentException("Meta no encontrada"));

        meta.setNombre(metaDTO.getNombre());
        meta.setDescripcion(metaDTO.getDescripcion());
        meta.setPesoObjetivo(metaDTO.getPesoObjetivo());

        metaRepository.save(meta);
        return new MetaDTO(meta);
    }
}