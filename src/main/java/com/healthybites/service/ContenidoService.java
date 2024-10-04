package com.healthybites.service;

import com.healthybites.model.entity.Contenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ContenidoService {
    Contenido createContenido(Contenido contenido);
    List<Contenido> getAllContenidoByUser(Integer userId);
    Contenido getContenidoById(Integer contenidoId);
    Contenido updateContenido(Integer contenidoId, Contenido contenido);
    void deleteContenido(Integer contenidoId);
}
