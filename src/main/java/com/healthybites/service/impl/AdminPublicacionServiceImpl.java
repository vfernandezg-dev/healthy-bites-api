package com.healthybites.service.impl;

import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Grupo;
import com.healthybites.model.entity.Publicacion;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.GrupoRepository;
import com.healthybites.repository.PublicacionRepository;
import com.healthybites.service.AdminPublicacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminPublicacionServiceImpl implements AdminPublicacionService {

    private final PublicacionRepository publicacionRepository;
    private final ClienteRepository clienteRepository;
    private final GrupoRepository grupoRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Publicacion> getAll() {
        return publicacionRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Publicacion> paginate(Pageable pageable) {
        return publicacionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Publicacion findById(Integer id) {
        return publicacionRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Publicacion no encontrada"));
    }

    @Transactional
    @Override
    public Publicacion create(Publicacion publicacion) {

        Cliente cliente = clienteRepository.findById(publicacion.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + publicacion.getCliente().getId()));
        Grupo grupo = grupoRepository.findById(publicacion.getGrupo().getId()).
                orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        publicacion.setCliente(cliente);
        publicacion.setGrupo(grupo);
        return publicacionRepository.save(publicacion);
    }

    @Transactional
    @Override
    public Publicacion update(Integer id, Publicacion updatePublicacion) {
        Publicacion publicacionFromDB = findById(id);

        Cliente cliente = clienteRepository.findById(updatePublicacion.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + updatePublicacion.getCliente().getId()));
        Grupo grupo = grupoRepository.findById(updatePublicacion.getGrupo().getId()).
                orElseThrow(() -> new RuntimeException("Grupo no encontrado"));

        publicacionFromDB.setTitulo(updatePublicacion.getTitulo());
        publicacionFromDB.setDescripcion(updatePublicacion.getDescripcion());
        publicacionFromDB.setFecha(updatePublicacion.getFecha());
        publicacionFromDB.setCliente(cliente);
        publicacionFromDB.setGrupo(grupo);
        return publicacionRepository.save(publicacionFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Publicacion publicacion = findById(id);
        publicacionRepository.delete(publicacion);
    }
}
