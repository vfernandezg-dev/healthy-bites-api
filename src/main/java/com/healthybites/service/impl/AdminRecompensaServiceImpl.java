package com.healthybites.service.impl;

import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Recompensa;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.RecompensaRepository;
import com.healthybites.service.AdminRecompensaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminRecompensaServiceImpl implements AdminRecompensaService {

    private final RecompensaRepository recompensaRepository;
    private final ClienteRepository clienteRepository;

    @Transactional(readOnly = true)
    @Override
    public List<Recompensa> getAll() {
        return recompensaRepository.findAll();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<Recompensa> paginate(Pageable pageable) {
        return recompensaRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    @Override
    public Recompensa findById(Integer id) {
        return recompensaRepository.findById(id).
                orElseThrow(() -> new RuntimeException("Recompensa no encontrada" + id));
    }

    @Transactional
    @Override
    public Recompensa create(Recompensa recompensa) {
        Cliente cliente = clienteRepository.findById(recompensa.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + recompensa.getCliente().getId()));
        recompensa.setCliente(cliente);
        return recompensaRepository.save(recompensa);
    }

    @Transactional
    @Override
    public Recompensa update(Integer id, Recompensa updateRecompensa) {
        Cliente cliente = clienteRepository.findById(updateRecompensa.getCliente().getId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado: " + updateRecompensa.getCliente().getId()));
        Recompensa recompensaFromDB = findById(id);
        recompensaFromDB.setNombre(updateRecompensa.getNombre());
        recompensaFromDB.setDescripcion(updateRecompensa.getDescripcion());
        recompensaFromDB.setDiasRequeridos(updateRecompensa.getDiasRequeridos());
        recompensaFromDB.setCliente(cliente);
        return recompensaRepository.save(recompensaFromDB);
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        Recompensa recompensa = findById(id);
        recompensaRepository.delete(recompensa);
    }
}
