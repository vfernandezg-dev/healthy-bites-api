package com.healthybites.service.impl;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ClienteMapper;
import com.healthybites.mapper.MetaMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Meta;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.MetaRepository;
import com.healthybites.service.AdminMetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Service
public class AdminMetaServiceImpl implements AdminMetaService{

    private final MetaRepository metaRepository;
    private final MetaMapper metaMapper;
    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;

    @Transactional(readOnly = true)
    @Override
    public List<MetaDTO> getAll() {
            List<Meta> metas = metaRepository.findAll();
            return metas.stream()
                    .map(metaMapper::toMetaDTO)
                    .toList();
    }

    @Transactional(readOnly = true)
    public Page<MetaDTO> paginate(Pageable pageable) {
        Page<Meta> metas = metaRepository.findAll(pageable);
        return metas.map(metaMapper::toMetaDTO);
    }

    @Transactional(readOnly = true)
    public MetaDTO findById(Integer id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("La meta con ID "+id+" no fue encontrado"));
        return metaMapper.toMetaDTO(meta);
    }


    @Transactional
    public MetaDTO create(MetaDTO metaDTO) {
        Meta meta = metaMapper.toEntity(metaDTO);

        Cliente cliente = clienteRepository.findById(metaDTO.getCliente().getId())
                .orElseThrow(() -> new BadRequestException("El cliente no fue encontrado"));



        ClienteDTO clienteDTO = clienteMapper.toDTO(cliente);

        cliente = clienteRepository.save(cliente);

        meta = metaRepository.save(meta);

        return metaMapper.toMetaDTO(meta);
    }


    @Transactional
    public MetaDTO update(Integer id, MetaDTO updateMetaDTO) {
        Meta metaFromDB = metaRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("La meta con ID "+id+" no fue encontrado"));

        metaRepository.findById(metaFromDB.getId())
                .filter(existingMeta -> !existingMeta.getId().equals(id))
                .ifPresent(existingMeta -> {
                   throw new BadRequestException("La meta ya existe con el mismo ID");
                });
        //Actualizar los campos
        metaFromDB.setNombre(updateMetaDTO.getNombre());
        metaFromDB.setDescripcion(updateMetaDTO.getDescripcion());
        metaFromDB.setPesoObjetivo(updateMetaDTO.getPesoObjetivo());
        metaFromDB = metaRepository.save(metaFromDB);
        return metaMapper.toMetaDTO(metaFromDB);
    }

    @Transactional
    public void delete(Integer id) {
        Meta meta = metaRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("La meta con ID "+id+" no fue encontrado"));
        metaRepository.delete(meta);
    }
}
