package com.healthybites.repository;

import com.healthybites.model.entity.Publicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PublicacionRepository extends JpaRepository<Publicacion, Integer> {
    List<Publicacion> findByClienteId(Integer clienteId);
    List<Publicacion> findByGrupoId(Integer grupoId);
    Optional<Publicacion> findByIdAndClienteId(Integer publicacionId, Integer clienteId);

    /** Publicaciones de grupos públicos y de los privados que el usuario creó o a los que pertenece. */
    @Query("""
            select p from Publicacion p left join p.grupo g left join g.cliente gc
            where g is null or g.esPrivado = false or gc.usuario.id = :usuarioId
                or exists (select u from Unirse u where u.grupo = g and u.cliente.usuario.id = :usuarioId)
            order by p.id""")
    List<Publicacion> findVisibleTo(@Param("usuarioId") Integer usuarioId);
}
