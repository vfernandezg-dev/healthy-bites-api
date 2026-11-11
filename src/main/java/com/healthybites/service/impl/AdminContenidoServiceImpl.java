package com.healthybites.service.impl;

import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.ContenidoDTO;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ContenidoMapper;
import com.healthybites.model.entity.Contenido;
import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import com.healthybites.repository.ContenidoRepository;
import com.healthybites.service.AdminContenidoService;
import com.healthybites.service.ContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AdminContenidoServiceImpl implements AdminContenidoService {

    private final ContenidoRepository contenidoRepository;
    private final ContenidoMapper contenidoMapper;
    private final ContenidoService contenidoService;

    @Transactional(readOnly = true)
    @Override
    public List<ContenidoDTO> getAll() {
        return contenidoRepository.findAll().stream()
                .map(contenidoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ContenidoDTO> paginate(Pageable pageable) {
        return contenidoRepository.findAll(pageable).map(contenidoMapper::toDTO);
    }

    @Transactional(readOnly = true)
    @Override
    public ContenidoDTO findById(Integer id) {
        return contenidoMapper.toDTO(getContenidoOrThrow(id));
    }

    @Transactional
    @Override
    public ContenidoDTO create(ContenidoCreateUpdateDTO contenidoDTO) {
        // Reutiliza la logica del flujo normal (slug unico, nutricionista y fechas)
        return contenidoService.create(contenidoDTO);
    }

    @Transactional
    @Override
    public ContenidoDTO update(Integer id, ContenidoCreateUpdateDTO updateContenidoDTO) {
        Contenido contenidoFromDB = getContenidoOrThrow(id);

        contenidoFromDB.setTitulo(updateContenidoDTO.getTitulo());
        contenidoFromDB.setDescripcion(updateContenidoDTO.getDescripcion());
        contenidoFromDB.setTipoContenido(updateContenidoDTO.getTipoContenido());
        contenidoFromDB.setCategoriaContenido(updateContenidoDTO.getCategoriaContenido());
        contenidoFromDB.setEsGratis(updateContenidoDTO.isEsGratis());
        contenidoFromDB.setFechaActualizacion(LocalDateTime.now());

        return contenidoMapper.toDTO(contenidoRepository.save(contenidoFromDB));
    }

    @Transactional
    @Override
    public void delete(Integer id) {
        contenidoRepository.delete(getContenidoOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Override
    public List<ContenidoDTO> obtenerContenido(TipoContenido tipoContenido, CategoriaContenido categoriaContenido) {
        return contenidoRepository.findAll().stream()
                .filter(contenido -> (tipoContenido == null || contenido.getTipoContenido() == tipoContenido) &&
                        (categoriaContenido == null || contenido.getCategoriaContenido() == categoriaContenido))
                .map(contenidoMapper::toDTO)
                .toList();
    }

    private Contenido getContenidoOrThrow(Integer id) {
        return contenidoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Contenido no encontrado: " + id));
    }
}
