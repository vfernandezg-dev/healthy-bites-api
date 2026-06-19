package com.healthybites.service;

import com.healthybites.model.entity.AccesoPlan;
import com.healthybites.model.entity.PlanAlimenticio;

import java.util.List;

public interface AccesoPlanService {
    AccesoPlan addPlanToCliente(Integer clienteId, Integer planId);
    void removePlanFromCliente(Integer clienteId, Integer planId);
    List<PlanAlimenticio> getPlanesInCliente(Integer clienteId);
}
