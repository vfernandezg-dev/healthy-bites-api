package com.healthybites.service.impl;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.mapper.ClienteMapper;
import com.healthybites.mapper.MetaMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Meta;
import com.healthybites.model.enums.EstadoMeta;
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

        Cliente cliente = metaFromDB.getCliente();
        cliente.getMetas().remove(metaFromDB);
        cliente.getMetas().add(metaFromDB);

        metaFromDB = metaRepository.save(metaFromDB);
        return metaMapper.toMetaDTO(metaFromDB);
    }


    @Transactional
    public void delete(Integer id) {
        Meta meta = metaRepository.findById(id)
                        .orElseThrow(()-> new ResourceNotFoundException("La meta con ID "+id+" no fue encontrado"));
        metaRepository.delete(meta);
    }

    @Transactional(readOnly = true)
    public String calcularRecomendacion(MetaDTO metaDTO) {
        double pesoObjetivo = metaDTO.getPesoObjetivo();
        double pesoActual = metaDTO.getCliente().getPeso();

        double diferenciaPeso = pesoActual - pesoObjetivo;
        String recomendacion;

        if (diferenciaPeso > 5) {
            recomendacion = "Se recomienda un plan de 6 meses para reducir gradualmente.";
        } else if (diferenciaPeso > 2) {
            recomendacion = "Se recomienda un plan de 3 meses.";
        } else {
            recomendacion = "Mantén tu peso actual con una dieta balanceada y ejercicio regular.";
        }

        return recomendacion;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MetaDTO> findMetasByClienteId(Integer clienteId) {
        List<Meta> metas = metaRepository.findByClienteId(clienteId);
        return metas.stream()
                .map(metaMapper::toMetaDTO)
                .toList();
    }
    @Override
    @Transactional
    public MetaDTO actualizarObjetivosSalud(Integer id, MetaDTO metaDTO) {
        Meta metaExistente = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La meta con ID " + id + " no fue encontrada"));

        // Actualizar los campos existentes
        metaExistente.setNombre(metaDTO.getNombre());
        metaExistente.setDescripcion(metaDTO.getDescripcion());
        metaExistente.setPesoObjetivo(metaDTO.getPesoObjetivo());

        metaRepository.save(metaExistente);
        return metaMapper.toMetaDTO(metaExistente);
    }


    @Override
    @Transactional
    public MetaDTO actualizacionEstadoMeta(Integer id, EstadoMeta nuevoEstado) {
        Meta metaFromDB = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La meta con ID " + id + " no fue encontrado"));

        // Verifica si la meta anterior estaba activa
        if (metaFromDB.getEstado() == EstadoMeta.ACTIVE) {
            // Cambia el estado de la meta anterior a COMPLETED
            metaFromDB.setEstado(EstadoMeta.COMPLETED);
            metaFromDB.getHistorialEstados().add(EstadoMeta.COMPLETED); // Agrega al historial
            metaRepository.save(metaFromDB); // Guarda el objetivo anterior como completado
        }

        // Cambia el estado de la meta existente al nuevo estado recibido
        metaFromDB.setEstado(nuevoEstado);
        metaFromDB.getHistorialEstados().add(nuevoEstado);
        metaRepository.save(metaFromDB); // Guarda el cambio de estado
        return metaMapper.toMetaDTO(metaFromDB);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EstadoMeta> getHistorialById(Integer id) {
        Meta meta = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La meta con ID " + id + " no fue encontrado"));
        return meta.getHistorialEstados(); // Devuelve el historial de estados
    }

    @Transactional
    public MetaDTO nuevoObjetivo(Integer id, EstadoMeta nuevoEstado) {
        Meta metaFromDB = metaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("La meta con ID " + id + " no fue encontrado"));

        // Verifica si la meta anterior estaba activa
        if (metaFromDB.getEstado() == EstadoMeta.ACTIVE) {
            // Cambia el estado de la meta anterior a COMPLETED
            metaFromDB.setEstado(EstadoMeta.COMPLETED);
            metaRepository.save(metaFromDB); // Guarda el objetivo anterior como completado
        }

        // Crea una nueva meta
        Meta newMeta = new Meta();
        newMeta.setNombre(metaFromDB.getNombre()); // Puede ser el mismo nombre o uno nuevo
        newMeta.setDescripcion(metaFromDB.getDescripcion());
        newMeta.setPesoObjetivo(metaFromDB.getPesoObjetivo()); // Asume que se mantiene el mismo
        newMeta.setCliente(metaFromDB.getCliente()); // Asigna el cliente existente
        newMeta.setEstado(EstadoMeta.ACTIVE); // Establece el nuevo objetivo como activo

        // Guarda la nueva meta
        metaRepository.save(newMeta);

        // Aquí puedes actualizar el perfil del cliente o el gráfico de progreso
        // Por ejemplo, actualiza el peso del cliente o algún otro dato necesario.

        return metaMapper.toMetaDTO(newMeta);
    }
    @Transactional(readOnly = true)
    public ClienteDTO getPerfilClienteByMetaId(Integer metaId) {
        Meta meta = metaRepository.findById(metaId)
                .orElseThrow(() -> new ResourceNotFoundException("La meta con ID " + metaId + " no fue encontrado"));
        Cliente cliente = meta.getCliente();
        return clienteMapper.ToDTO(cliente);
    }
}
