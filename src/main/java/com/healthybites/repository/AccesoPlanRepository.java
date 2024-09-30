package com.healthybites.repository;

import com.healthybites.model.entity.AccesoPlan;
import com.healthybites.model.entity.AccesoPlanPK;
import com.healthybites.model.entity.PlanAlimenticio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface AccesoPlanRepository extends JpaRepository<AccesoPlan, AccesoPlanPK> {

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO acceso_plan (cliente, plan_alimenticio) VALUES (:clienteId, :planId)", nativeQuery = true)
    void addPlanToCliente(@Param("clienteId") Integer clienteId, @Param("planId") Integer planId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM acceso_plan WHERE cliente = :clienteId AND plan_alimenticio = :planId", nativeQuery = true)
    void deleteByPlanAAndCliente(@Param("clienteId") Integer clienteId, @Param("planId") Integer planId);

    @Query(value = "SELECT p FROM PlanAlimenticio p JOIN AccesoPlan a ON p.id = a.planAlimenticio WHERE a.cliente = :clienteId")
    List<PlanAlimenticio> findPlanAlimenticioByCliente(@Param("clienteId") Integer clienteId);
}
