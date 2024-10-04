package com.healthybites.repository;

import com.healthybites.model.entity.Contenido;
import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.List;

public interface ContenidoRepository extends JpaRepository<Contenido, Integer> {

    @Query("SELECT c FROM Contenido c WHERE (:tipoContenido IS NULL OR c.tipoContenido = :tipoContenido) " +
            "AND (:categoriaContenido IS NULL OR c.categoriaContenido = :categoriaContenido)")
    List<Contenido> findByTipoYCategoria(@Param("tipoContenido") TipoContenido tipoContenido,
                                         @Param("categoriaContenido") CategoriaContenido categoriaContenido);
}
