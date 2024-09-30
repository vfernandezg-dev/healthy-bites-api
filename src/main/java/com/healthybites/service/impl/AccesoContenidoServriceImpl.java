package com.healthybites.service.impl;

import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Contenido;
import com.healthybites.repository.AccesoContenidoRepository;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.service.AccesoContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccesoContenidoServriceImpl implements AccesoContenidoService {

    private final AccesoContenidoRepository accesoContenidoRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public AccesoContenido addContenidoToCliente(Integer clienteId, Integer contenidoId) {
        accesoContenidoRepository.addClienteToContenido(clienteId, contenidoId);

        AccesoContenido accesoContenido = new AccesoContenido();
        accesoContenido.setCliente(clienteId);
        accesoContenido.setContenido(contenidoId);
        return accesoContenido;
    }

    @Override
    public void removeContenidoFromCliente(Integer clienteId, Integer contenidoId) {
        accesoContenidoRepository.deleteByClienteAndContenido(clienteId, contenidoId);
    }

    @Override
    public List<Contenido> getContenidoInCliente(Integer clienteId) {
        return accesoContenidoRepository.findContenidoByCliente(clienteId);
    }
}