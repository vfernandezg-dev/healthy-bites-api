package com.healthybites.service.impl;

import com.healthybites.dto.SuscripcionCreateUpdateDTO;
import com.healthybites.dto.SuscripcionDetailsDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.SuscripcionMapper;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.entity.Usuario;
import com.healthybites.model.enums.EstadoPago;
import com.healthybites.repository.SuscripcionRepository;
import com.healthybites.repository.UsuarioRepository;
import com.healthybites.service.SuscripcionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class SuscripcionServiceImpl implements SuscripcionService {

    private final SuscripcionRepository suscripcionRepository;
    private final UsuarioRepository usuarioRepository;
    private final SuscripcionMapper suscripcionMapper;

    @Transactional(readOnly = true)
    @Override
    public List<SuscripcionDetailsDTO> getAll() {
        List<Suscripcion> suscripciones = suscripcionRepository.findAll();
        return suscripciones.stream()
                .map(suscripcionMapper::toDTO)
                .toList();
    }

    @Override
    public SuscripcionDetailsDTO confirmSuscripcion(Integer id) {
        Suscripcion suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripcion con id " + id + " no encontrado"));

        suscripcion.setEstadoPago(EstadoPago.PAGADO);

        // Guarda la suscripcion
        Suscripcion updatedSuscripcion = suscripcionRepository.save(suscripcion);
        return suscripcionMapper.toDTO(updatedSuscripcion);
    }

    @Override
    public SuscripcionDetailsDTO getSuscripcionId(Integer id) {
        Suscripcion suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripcion con id " + id + " no encontrado"));

        return suscripcionMapper.toDTO(suscripcion);
    }

    @Transactional(readOnly = true)
    @Override
    public SuscripcionDetailsDTO findById(Integer id) {
        Suscripcion suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripcion con id " + id + " no encontrado"));
        return suscripcionMapper.toDTO(suscripcion);
    }

    @Override
    public SuscripcionDetailsDTO create(SuscripcionCreateUpdateDTO suscripcionCreateUpdateDTO) {
        Suscripcion suscripcion = suscripcionMapper.toEntity(suscripcionCreateUpdateDTO);

        // Recuperar el usuario del repositorio usando el id_cliente
        Usuario usuario = usuarioRepository.findById(suscripcionCreateUpdateDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + suscripcionCreateUpdateDTO.getUsuarioId() + " no encontrado"));

        suscripcion.setUsuario(usuario);

        switch (suscripcion.getTipoSuscripcion()) {
            case BASICO -> {
                suscripcion.setPrecio(0.00);
                suscripcion.setEstadoPago(EstadoPago.PAGADO);
            }
            case PREMIUM -> {
                suscripcion.setPrecio(5.99);
                suscripcion.setEstadoPago(EstadoPago.PENDIENTE);
            }
            case VIP -> {
                suscripcion.setPrecio(9.99);
                suscripcion.setEstadoPago(EstadoPago.PENDIENTE);
            }
        }

        suscripcion.setFechaCreacion(LocalDateTime.now());

        // Guardar la suscripcion
        Suscripcion suscripcionSaved = suscripcionRepository.save(suscripcion);

        // Retornar la suscripcion guardada
        return suscripcionMapper.toDTO(suscripcionSaved);
    }

    @Override
    public void delete(Integer id) {
        Suscripcion suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripcion con id " + id + " no encontrado"));
        suscripcionRepository.delete(suscripcion);
    }
}
