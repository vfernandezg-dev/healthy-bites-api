package com.healthybites.service.impl;

import com.healthybites.dto.ContenidoDTO;
import com.healthybites.mapper.ContenidoMapper;
import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Contenido;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.repository.AccesoContenidoRepository;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.ContenidoRepository;
import com.healthybites.service.AccesoContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

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
    public ContenidoDTO addContentToClient(Integer clientId, Integer contentId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Contenido contenido = contenidoRepository.findById(contentId)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));

        Suscripcion suscripcion = cliente.getUsuario().getSuscripcion();

        // Contenido de pago: solo con una suscripción PREMIUM o VIP pagada. Antes, sin suscripción (o sin pagar)
        // se concedía el acceso.
        if (!contenido.isEsGratis() && !Suscripcion.isPremiumActiva(suscripcion)) {
            throw new AccessDeniedException("Necesitas una suscripción Premium o VIP pagada para este contenido");
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
    public List<ContenidoDTO> getAllContentByClient(Integer clientId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Suscripcion suscripcion = cliente.getUsuario().getSuscripcion();

        List<Contenido> contenidos;

        if (!Suscripcion.isPremiumActiva(suscripcion)) {
            // Sin suscripción premium pagada: contenidos gratuitos y los desbloqueados por el cliente
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


    @Override
    public boolean isClientePremiumOrVip(Integer clientId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Antes fallaba con NullPointerException si el usuario no tenía suscripción y no miraba si estaba pagada.
        return Suscripcion.isPremiumActiva(cliente.getUsuario().getSuscripcion());
    }
}

