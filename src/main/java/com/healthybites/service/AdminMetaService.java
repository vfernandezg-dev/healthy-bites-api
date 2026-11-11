package com.healthybites.service;

import com.healthybites.dto.ClienteDetailsDTO;
import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.model.enums.EstadoMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminMetaService {
    List<MetaDTO> getAll();
    Page<MetaDTO> paginate(Pageable pageable);
    MetaDTO findById(Integer id);
    MetaDTO create(MetaCreateDTO metaCreateDTO);
    MetaDTO update(Integer id, MetaCreateDTO updateMetaDTO);
    void delete(Integer id);
    String calcularRecomendacion(MetaCreateDTO metaCreateDTO);
    List<MetaDTO> findMetasByClienteId(Integer clienteId);
    MetaDTO actualizarObjetivosSalud(Integer id, MetaCreateDTO metaCreateDTO);
    MetaDTO actualizacionEstadoMeta(Integer id, EstadoMeta nuevoEstado);
    List<EstadoMeta> getHistorialById(Integer id);
    ClienteDetailsDTO getPerfilClienteByMetaId(Integer metaId);
}
