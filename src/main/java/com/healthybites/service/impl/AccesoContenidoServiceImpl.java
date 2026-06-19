package com.healthybites.service.impl;

import com.healthybites.dto.ContenidoDetailsDTO;
import com.healthybites.mapper.ContenidoMapper;
import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Contenido;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.enums.TipoSuscripcion;
import com.healthybites.repository.AccesoContenidoRepository;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.ContenidoRepository;
import com.healthybites.service.AccesoContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.ref.PhantomReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccesoContenidoServiceImpl implements AccesoContenidoService {

    private final AccesoContenidoRepository accesoContenidoRepository;
    private final ContenidoRepository contenidoRepository;
    private final ContenidoMapper contenidoMapper;
    private final ClienteRepository clienteRepository;

    @Override
    public ContenidoDetailsDTO addContentToClient(Integer clientId, Integer contentId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Contenido contenido = contenidoRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));

        Suscripcion suscripcion = cliente.getUsuario().getSuscripcion();

        // Validación de acceso de acuerdo al tipo de suscripción y si el contenido es gratuito o no
        if (suscripcion != null
                && suscripcion.getTipoSuscripcion().equals(TipoSuscripcion.BASICO)
                && !contenido.isEsGratis()) {
            throw new RuntimeException("No se puede acceder a contenido premium con suscripción básica");
        }

        LocalDateTime fecha = LocalDateTime.now();
        accesoContenidoRepository.addContenidoToCliente(clientId, contentId, fecha);

        return contenidoMapper.toDTO(contenido);
    }

    @Override
    public void removeContentFromClient(Integer clientId, Integer contentId) {
        accesoContenidoRepository.deleteByClienteAndContenido(clientId, contentId);
    }

    @Override
    public List<ContenidoDetailsDTO> getAllContentByClient(Integer clientId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Suscripcion suscripcion = cliente.getUsuario().getSuscripcion();

        List<Contenido> contenidos;

        if (suscripcion != null && suscripcion.getTipoSuscripcion().equals(TipoSuscripcion.BASICO)) {
            // Contenidos gratuitos y específicos del cliente en suscripción básica
            contenidos = contenidoRepository.findByEsGratisTrue();
            contenidos.addAll(accesoContenidoRepository.findByCliente(cliente)
                    .stream()
                    .map(AccesoContenido::getContenido)
                    .collect(Collectors.toList()));
        } else {
            // Contenidos asignados al cliente para otras suscripciones
            contenidos = accesoContenidoRepository.findByCliente(cliente)
                    .stream()
                    .map(AccesoContenido::getContenido)
                    .collect(Collectors.toList());
        }

        return contenidos.stream()
                .map(contenidoMapper::toDTO)
                .collect(Collectors.toList());
    }

}

