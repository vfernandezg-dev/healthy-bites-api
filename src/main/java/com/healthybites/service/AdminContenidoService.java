package com.healthybites.service;

import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.ContenidoDTO;
import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

// Servicio administrativo de contenidos (todavia sin controller expuesto).
public interface AdminContenidoService {
    List<ContenidoDTO> getAll();
    Page<ContenidoDTO> paginate(Pageable pageable);
    ContenidoDTO findById(Integer id);
    ContenidoDTO create(ContenidoCreateUpdateDTO contenido);
    ContenidoDTO update(Integer id, ContenidoCreateUpdateDTO updateContenidoDTO);
    void delete(Integer id);

    // Método para obtener contenido filtrado
    List<ContenidoDTO> obtenerContenido(TipoContenido tipoContenido, CategoriaContenido categoriaContenido);
}
