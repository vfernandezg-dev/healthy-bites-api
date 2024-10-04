package com.healthybites.service.impl;

import com.healthybites.dto.ContenidoDTO;
import com.healthybites.mapper.ContenidoMapper;
import com.healthybites.model.entity.Contenido;
import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import com.healthybites.repository.ContenidoRepository;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.service.AdminContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AdminContenidoServiceImpl implements AdminContenidoService {

    @Autowired
    private final ContenidoRepository contenidoRepository;
    private final NutricionistaRepository nutricionistaRepository;

    @Autowired
    private final ContenidoMapper contenidoMapper;

    @Transactional(readOnly = true)
    @Override
    public List<ContenidoDTO> getAll() {
        List<Contenido> contenidos = contenidoRepository.findAll();
        return contenidos.stream()
                .map(contenidoMapper::toDto) // Convertir entidad a DTO
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ContenidoDTO> paginate(Pageable pageable) {
        Page<Contenido> contenidos = contenidoRepository.findAll(pageable);
        return contenidos.map(contenidoMapper::toDto); // Convertir entidad a DTO
    }

    @Transactional(readOnly = true)
    @Override
    public ContenidoDTO findById(Integer id) {
        Contenido contenido = contenidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
        return contenidoMapper.toDto(contenido); // Convertir entidad a DTO
    }

    @Transactional
    @Override
    public ContenidoDTO create(ContenidoDTO contenidoDTO) {
        Contenido contenido = contenidoMapper.toEntity(contenidoDTO); // Convertir DTO a entidad
        contenido = contenidoRepository.save(contenido);
        return contenidoMapper.toDto(contenido); // Convertir entidad guardada a DTO
    }

    @Transactional
    @Override
    public ContenidoDTO update(Integer id, ContenidoDTO updateContenidoDTO) {
        Contenido contenidoFromDB = contenidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));

        contenidoFromDB.setTitulo(updateContenidoDTO.getTitulo());
        contenidoFromDB.setDescripcion(updateContenidoDTO.getDescripcion());
        contenidoFromDB.setTipoContenido(updateContenidoDTO.getTipoContenido());
        contenidoFromDB.setCategoriaContenido(updateContenidoDTO.getCategoriaContenido());
        contenidoFromDB.setEsGratis(updateContenidoDTO.isEsGratis());

        Contenido updatedContenido = contenidoRepository.save(contenidoFromDB);
        return contenidoMapper.toDto(updatedContenido); // Convertir entidad actualizada a DTO
    }

    @Override
    public void delete(Integer id) {
        Contenido contenido = contenidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Contenido no encontrado"));
        contenidoRepository.delete(contenido);
    }

    @Override
    public List<ContenidoDTO> obtenerContenido(TipoContenido tipoContenido, CategoriaContenido categoriaContenido) {
        // Filtrar contenidos utilizando el repositorio
        List<Contenido> contenidos = contenidoRepository.findAll(); // Obtener todos los contenidos

        return contenidos.stream()
                .filter(contenido -> (tipoContenido == null || contenido.getTipoContenido() == tipoContenido) &&
                        (categoriaContenido == null || contenido.getCategoriaContenido() == categoriaContenido))
                .map(contenidoMapper::toDto) // Convertir a DTO
                .collect(Collectors.toList());
    }
}




