package com.healthybites.service;

import com.healthybites.dto.PlanAlimenticioDTO;

import java.util.List;

public interface AccesoPlanService {
    // Metodo para añadir un plan a un cliente
    PlanAlimenticioDTO addPlanToClient(Integer clientId, Integer planId);

    // Metodo para eliminar un plan de un cliente
    void removePlanFromClient(Integer clientId, Integer planId);

    // Metodo para obtener todos los planes de un cliente
    List<PlanAlimenticioDTO> getAllPlansByClient(Integer clientId);

    boolean isClientePremiumOrVip(Integer clientId);
}
