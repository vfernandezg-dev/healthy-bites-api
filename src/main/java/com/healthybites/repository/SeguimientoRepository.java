package com.healthybites.repository;

import com.healthybites.model.entity.Seguimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeguimientoRepository extends JpaRepository<Seguimiento, Integer> {
    List<Seguimiento> findByMetaId(Integer metaId);
    Optional<Seguimiento> findByIdAndMetaId(Integer id, Integer metaId);

    List<Seguimiento> findByMetaIdAndMetaClienteId(Integer metaId, Integer clienteId);
    Page<Seguimiento> findByMetaIdAndMetaClienteId(Integer metaId, Integer clienteId, Pageable pageable);
}
