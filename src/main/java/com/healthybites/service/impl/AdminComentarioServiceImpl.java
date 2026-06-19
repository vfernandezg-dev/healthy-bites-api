package com.healthybites.service.impl;

import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Comentario;
import com.healthybites.model.entity.Publicacion;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.ComentarioRepository;
import com.healthybites.repository.PublicacionRepository;
import com.healthybites.service.AdminComentarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminComentarioServiceImpl implements AdminComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final ClienteRepository clienteRepository;
    private final PublicacionRepository publicacionRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Comentario> getAll() {
        return comentarioRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Comentario> paginate(Pageable pageable) {
        return comentarioRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Comentario findById(Integer id) {
        return comentarioRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Comentario no encontrado"));
    }

    @Transactional
    @Override
    public Comentario create(Comentario comentario) {
        Cliente cliente = clienteRepository.findById(comentario.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + comentario.getCliente().getId()));
        Publicacion publicacion = publicacionRepository.findById(comentario.getPublicacion().getId()).
                orElseThrow(() -> new RuntimeException("Publicacion no encontrada por ID: " + comentario.getPublicacion().getId()));

        comentario.setCliente(cliente);
        comentario.setPublicacion(publicacion);
        return comentarioRepository.save(comentario);
    }

    @Transactional
    @Override
    public Comentario update(Integer id, Comentario updateComentario) {
        Comentario comentarioFromDB = findById(id);
        Cliente cliente = clienteRepository.findById(updateComentario.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + updateComentario.getCliente().getId()));
        Publicacion publicacion = publicacionRepository.findById(updateComentario.getPublicacion().getId()).
                orElseThrow(() -> new RuntimeException("Publicacion no encontrada por ID: " + updateComentario.getPublicacion().getId()));

        comentarioFromDB.setMensaje(updateComentario.getMensaje());
        comentarioFromDB.setCliente(cliente);
        comentarioFromDB.setPublicacion(publicacion);
        return comentarioRepository.save(comentarioFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Comentario comentario = comentarioRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Comentario no encontrado por ID: " + id));
        comentarioRepository.delete(comentario);

    }
}
