package com.healthybites.service.impl;

import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.mapper.PlanAlimenticioMapper;
import com.healthybites.model.entity.AccesoPlan;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.model.enums.TipoSuscripcion;
import com.healthybites.repository.AccesoPlanRepository;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.AccesoPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.lang.ref.PhantomReference;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccesoPlanServiceImpl implements AccesoPlanService {

    private final AccesoPlanRepository accesoPlanRepository;
    private final PlanAlimenticioRepository planAlimenticioRepository;
    private final ClienteRepository clienteRepository;
    private final PlanAlimenticioMapper planAlimenticioMapper;

    @Override
    public PlanAlimenticioDTO addPlanToClient(Integer clientId, Integer planId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        PlanAlimenticio plan = planAlimenticioRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan no encontrado"));

        Suscripcion suscripcion = cliente.getUsuario().getSuscripcion();

        // Validación de acceso de acuerdo al tipo de suscripción y si el plan es gratuito o no
        if (suscripcion != null
                && suscripcion.getTipoSuscripcion().equals(TipoSuscripcion.BASICO)
                && !plan.isEsGratis()) {
            throw new RuntimeException("No se puede acceder a planes premium con suscripción básica");
        }

        LocalDateTime fecha = LocalDateTime.now();
        accesoPlanRepository.addPlanToCliente(clientId, planId, fecha);

        return planAlimenticioMapper.toDTO(plan);
    }

    @Override
    public void removePlanFromClient(Integer clientId, Integer planId) {
        boolean planPertenece = accesoPlanRepository.existsByClienteIdAndPlanId(clientId, planId);

        if (!planPertenece) {
            throw new IllegalArgumentException("El plan no pertenece al cliente");
        }

        accesoPlanRepository.deleteByClienteAndPlan(clientId, planId);
    }

    @Override
    public List<PlanAlimenticioDTO> getAllPlansByClient(Integer clientId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        List<PlanAlimenticio> planes = accesoPlanRepository.findAccesoPlanByCliente(cliente);

        return planes.stream()
                .map(planAlimenticioMapper::toDTO)
                .collect(Collectors.toList());
    }
}

