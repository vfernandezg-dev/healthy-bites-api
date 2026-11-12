package com.healthybites.service.impl;

import com.healthybites.dto.SuscripcionCreateUpdateDTO;
import com.healthybites.dto.SuscripcionDetailsDTO;
import com.healthybites.exception.ConflictException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.SuscripcionMapper;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.entity.Usuario;
import com.healthybites.model.enums.EstadoPago;
import com.healthybites.model.enums.TipoSuscripcion;
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

    /**
     * Alta o cambio de plan. Cada usuario tiene una sola fila (restricción única en {@code id_usuario}); antes un
     * segundo alta chocaba con ella y quien tenía el plan básico no podía pasar a uno de pago. El plan básico queda
     * activo al momento; uno de pago queda pendiente hasta que se captura el pago en PayPal.
     */
    @Override
    @Transactional
    public SuscripcionDetailsDTO create(SuscripcionCreateUpdateDTO suscripcionCreateUpdateDTO) {
        Usuario usuario = usuarioRepository.findById(suscripcionCreateUpdateDTO.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario con id " + suscripcionCreateUpdateDTO.getUsuarioId() + " no encontrado"));
        TipoSuscripcion tipo = suscripcionCreateUpdateDTO.getTipoSuscripcion();

        Suscripcion suscripcion = suscripcionRepository.findByUsuarioId(usuario.getId()).orElseGet(Suscripcion::new);
        if (suscripcion.getId() != null && suscripcion.getTipoSuscripcion() == tipo
                && suscripcion.getEstadoPago() == EstadoPago.PAGADO) {
            throw new ConflictException("Ya tienes la suscripción " + tipo);
        }

        suscripcion.setUsuario(usuario);
        suscripcion.setTipoSuscripcion(tipo);
        suscripcion.setPrecio(Suscripcion.precioDe(tipo));
        suscripcion.setEstadoPago(tipo == TipoSuscripcion.BASICO ? EstadoPago.PAGADO : EstadoPago.PENDIENTE);
        suscripcion.setPaypalOrderId(null);
        suscripcion.setFechaCreacion(LocalDateTime.now());

        return suscripcionMapper.toDTO(suscripcionRepository.save(suscripcion));
    }

    @Override
    public void delete(Integer id) {
        Suscripcion suscripcion = suscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suscripcion con id " + id + " no encontrado"));
        suscripcionRepository.delete(suscripcion);
    }
}
