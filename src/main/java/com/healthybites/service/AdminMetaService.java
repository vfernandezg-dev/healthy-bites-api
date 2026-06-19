package com.healthybites.service;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.model.enums.EstadoMeta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AdminMetaService {
    List<MetaDTO> getAll();
    Page<MetaDTO> paginate(Pageable pageable);
    MetaDTO findById(Integer id);
    MetaDTO create(MetaDTO metaDTO);
    MetaDTO update(Integer id, MetaDTO updateMetaDTO);
    void delete(Integer id);
    String calcularRecomendacion(MetaDTO metaDTO);
    List<MetaDTO> findMetasByClienteId(Integer clienteId);
    MetaDTO actualizarObjetivosSalud(Integer id, MetaDTO metaDTO);
    public MetaDTO actualizacionEstadoMeta(Integer id, EstadoMeta nuevoEstado);
    List<EstadoMeta> getHistorialById(Integer id);
    ClienteDTO getPerfilClienteByMetaId(Integer metaId);
}
