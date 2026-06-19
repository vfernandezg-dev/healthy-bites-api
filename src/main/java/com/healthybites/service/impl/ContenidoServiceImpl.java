package com.healthybites.service.impl;

import com.healthybites.model.entity.Contenido;
import com.healthybites.model.entity.Nutricionista;
import com.healthybites.repository.ContenidoRepository;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.service.ContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class ContenidoServiceImpl implements ContenidoService {

    private final ContenidoRepository contenidoRepository;
    private final NutricionistaRepository nutricionistaRepository;

    @Transactional
    @Override
    public Contenido createContenido(Contenido contenido) {

        Nutricionista nutricionista = nutricionistaRepository.findById(contenido.getNutricionista().getId())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado por ID: " + contenido.getNutricionista().getId()));

        return contenidoRepository.save(contenido);
    }

    @Transactional(readOnly = true)
    @Override
    public List<Contenido> getAllContenidoByUser(Integer userId) {
        return contenidoRepository.findByNutricionistaId(userId);
    }

    @Transactional(readOnly = true)
    @Override
    public Contenido getContenidoById(Integer contenidoId) {
        return contenidoRepository.findById(contenidoId).
                orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
    }

    @Transactional
    @Override
    public Contenido updateContenido(Integer contenidoId, Contenido contenido) {
        Contenido contenidoFromDB = getContenidoById(contenidoId);

        Nutricionista nutricionista = nutricionistaRepository.findById(contenido.getNutricionista().getId())
                .orElseThrow(() -> new RuntimeException("Nutricionista no encontrado por ID: " + contenido.getNutricionista().getId()));

        contenidoFromDB.setTitulo(contenido.getTitulo());
        contenidoFromDB.setDescripcion(contenido.getDescripcion());
        contenidoFromDB.setTipoContenido(contenido.getTipoContenido());
        contenidoFromDB.setCategoriaContenido(contenido.getCategoriaContenido());
        contenidoFromDB.setEsGratis(contenido.isEsGratis());
        contenidoFromDB.setNutricionista(contenido.getNutricionista());
        return contenidoRepository.save(contenidoFromDB);
    }

    @Transactional
    @Override
    public void deleteContenido(Integer contenidoId) {
        Contenido contenido = getContenidoById(contenidoId);
        contenidoRepository.delete(contenido);
    }
}
