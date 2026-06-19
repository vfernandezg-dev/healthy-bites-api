package com.healthybites.service.impl;

import com.healthybites.model.entity.AccesoPlan;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.repository.AccesoPlanRepository;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.service.AccesoPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AccesoPlanServiceImpl implements AccesoPlanService {

    private final AccesoPlanRepository accesoPlanRepository;
    private final ClienteRepository clienteRepository;

    @Override
    public AccesoPlan addPlanToCliente(Integer clienteId, Integer planId) {
        accesoPlanRepository.addPlanToCliente(clienteId, planId);

        AccesoPlan accesoPlan = new AccesoPlan();
        accesoPlan.setCliente(clienteId);
        accesoPlan.setPlanAlimenticio(planId);
        return accesoPlan;
    }

    @Override
    public void removePlanFromCliente(Integer clienteId, Integer planId) {
        accesoPlanRepository.deleteByPlanAAndCliente(clienteId, planId);
    }

    @Override
    public List<PlanAlimenticio> getPlanesInCliente(Integer clienteId) {
        return accesoPlanRepository.findPlanAlimenticioByCliente(clienteId);
    }
}
