package com.healthybites.service.impl;

import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.enums.EstadoPago;
import com.healthybites.model.enums.EstadoSuscripcion;
import com.healthybites.model.enums.TipoSuscripcion;
import com.healthybites.repository.SuscripcionRepository;
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

    @Override
    @Transactional
    public Suscripcion createSuscripcion(Suscripcion suscripcion) {

        //Establecer un estado de suscripcion ACTIVA
        suscripcion.setEstadoSuscripcion(EstadoSuscripcion.ACTIVA);

        //Establecer un estado de pago PENDIENTE
        if (suscripcion.getTipoSuscripcion() == TipoSuscripcion.BASICO) {
            suscripcion.setEstadoPago(EstadoPago.PAGADO);
        } else {
            suscripcion.setEstadoPago(EstadoPago.PENDIENTE);
        }

        //Establecer una fecha de creacion
        suscripcion.setFechaInicio(LocalDateTime.now());

        //Establecer una fecha de expiracion
        if (suscripcion.getTipoSuscripcion() != TipoSuscripcion.BASICO) {
            suscripcion.setFechaFin(LocalDateTime.now().plusMonths(1));
        } else {
            suscripcion.setFechaFin(LocalDateTime.now().plusYears(100));
        }

        switch (suscripcion.getTipoSuscripcion()) {
            case BASICO:
                suscripcion.setPrecio(0.0);
                break;
            case PREMIUM:
                suscripcion.setPrecio(9.99);
                break;
            default:
                throw new IllegalArgumentException("Tipo de suscripción no válido: " + suscripcion.getTipoSuscripcion());
        }

        return suscripcionRepository.save(suscripcion);
    }

    @Override
    public List<Suscripcion> getSuscripcionHistoryByUserId(Integer id) {
        return suscripcionRepository.findByClienteId(id);
    }

    /*private final SuscripcionRepository suscripcionRepository;
    private final ClienteRepository clienteRepository;
    private final PagoRepository pagoRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Suscripcion> getAll() {
        return suscripcionRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Suscripcion> paginate(Pageable pageable) {
        return suscripcionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Suscripcion findById(Integer id) {
        return suscripcionRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Suscripcion no encontrada"));
    }

    @Transactional
    @Override
    public Suscripcion create(Suscripcion suscripcion) {
        Pago pago = pagoRepository.findById(suscripcion.getPago().getId()).
                orElseThrow(() -> new RuntimeException("Pago no encontrado por ID: " + suscripcion.getPago().getId()));
        Cliente cliente = clienteRepository.findById(suscripcion.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + suscripcion.getCliente().getId()));

        suscripcion.setPago(pago);
        suscripcion.setCliente(cliente);
        return suscripcionRepository.save(suscripcion);
    }

    @Transactional
    @Override
    public Suscripcion update(Integer id, Suscripcion updateSuscripcion) {
        Suscripcion suscripcionFromDB = findById(id);

        Pago pago = pagoRepository.findById(updateSuscripcion.getPago().getId()).
                orElseThrow(() -> new RuntimeException("Pago no encontrado por ID: " + updateSuscripcion.getPago().getId()));
        Cliente cliente = clienteRepository.findById(updateSuscripcion.getCliente().getId()).
                orElseThrow(() -> new RuntimeException("Cliente no encontrado por ID: " + updateSuscripcion.getCliente().getId()));

        suscripcionFromDB.setTipoSuscripcion(updateSuscripcion.getTipoSuscripcion());
        suscripcionFromDB.setPrecio(updateSuscripcion.getPrecio());
        suscripcionFromDB.setFechaInicio(updateSuscripcion.getFechaInicio());
        suscripcionFromDB.setFechaFin(updateSuscripcion.getFechaFin());
        suscripcionFromDB.setPago(pago);
        suscripcionFromDB.setCliente(cliente);
        return suscripcionRepository.save(suscripcionFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Suscripcion suscripcion = suscripcionRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Suscripcion no encontrada por ID: " + id));
        suscripcionRepository.delete(suscripcion);
    }*/
}
