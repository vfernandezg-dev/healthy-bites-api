package com.healthybites.repository;

import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Grupo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface GrupoRepository extends JpaRepository<Grupo, Integer> {
    List<Grupo> findByClienteId(Integer clienteId);
    Optional<Grupo> findByIdAndClienteId(Integer grupoId, Integer clienteId);

    /** Grupos públicos más los privados que el usuario creó o a los que pertenece. */
    @Query("""
            select g from Grupo g left join g.cliente gc
            where g.esPrivado = false or gc.usuario.id = :usuarioId
                or exists (select u from Unirse u where u.grupo = g and u.cliente.usuario.id = :usuarioId)
            order by g.id""")
    List<Grupo> findVisibleTo(@Param("usuarioId") Integer usuarioId);

    /** Suma o resta miembros con un UPDATE atómico, sin leer y escribir el valor. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Grupo g set g.cantidadMiembros = g.cantidadMiembros + :delta where g.id = :id")
    int adjustMemberCount(@Param("id") Integer grupoId, @Param("delta") int delta);
}
