package com.healthybites.repository;

import com.healthybites.model.entity.AccesoPlan;
import com.healthybites.model.entity.AccesoPlanPK;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.PlanAlimenticio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

public interface AccesoPlanRepository extends JpaRepository<AccesoPlan, AccesoPlanPK> {

    // Esta query es para añadir plan a cliente
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO acceso_plan (cliente_id, plan_alimenticio_id, fecha_creacion)" +
            "VALUES (:clienteId, :planId, :fechaCreacionId)", nativeQuery = true)
    void addPlanToCliente(@Param("clienteId") Integer clienteId,@Param("planId") Integer planId, @Param("fechaCreacionId") LocalDateTime fechaCreacion);

    // Esta query es para eliminar plan de cliente
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM acceso_plan WHERE cliente_id = :clienteId AND plan_alimenticio_id = :planId", nativeQuery = true)
    void deleteByClienteAndPlan(@Param("clienteId") Integer cliente,@Param("planId") Integer plan);

    // Esta query es para listar todos los planes por cliente
    @Query("SELECT ap.planAlimenticio FROM AccesoPlan ap WHERE ap.cliente = :clienteId ")
    List<PlanAlimenticio> findAccesoPlanByCliente(Cliente clienteId);

    // Esta query es para verificar si un cliente tiene acceso a un plan
    @Query("SELECT COUNT(ap) > 0 FROM AccesoPlan ap WHERE ap.cliente.id = :clienteId AND ap.planAlimenticio.id = :planId")
    boolean existsByClienteIdAndPlanId(@Param("clienteId") Integer clienteId, @Param("planId") Integer planId);

}
