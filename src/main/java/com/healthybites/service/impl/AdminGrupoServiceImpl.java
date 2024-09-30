package com.healthybites.service.impl;

import com.healthybites.model.entity.Grupo;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.service.AdminClienteService;
import com.healthybites.service.AdminGrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminGrupoServiceImpl implements AdminGrupoService {

    private final GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Grupo> getAll() {
        return grupoRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Grupo> paginate(Pageable pageable) {
        return grupoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Grupo findById(Integer id) {
        return grupoRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Grupo no encontrado"));
    }

    @Transactional
    @Override
    public Grupo create(Grupo grupo) {
        return grupoRepository.save(grupo);
    }

    @Transactional
    @Override
    public Grupo update(Integer id, Grupo updateGrupo) {
        Grupo grupoFromDB = findById(id);
        grupoFromDB.setNombre(updateGrupo.getNombre());
        grupoFromDB.setEsPrivado(updateGrupo.isEsPrivado());
        return grupoRepository.save(grupoFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Grupo grupo = findById(id);
        grupoRepository.delete(grupo);
    }
}
