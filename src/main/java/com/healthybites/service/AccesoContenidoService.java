package com.healthybites.service;

import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.Contenido;

import java.util.List;

public interface AccesoContenidoService {
    AccesoContenido addContenidoToCliente(Integer clienteId, Integer contenidoId);
    void removeContenidoFromCliente(Integer clienteId, Integer contenidoId);
    List<Contenido> getContenidoInCliente(Integer clienteId);
}
