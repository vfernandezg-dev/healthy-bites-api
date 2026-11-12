package com.healthybites.service.impl;

import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.mapper.PlanAlimenticioMapper;
import com.healthybites.model.entity.AccesoPlan;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.model.entity.Suscripcion;
import com.healthybites.repository.AccesoPlanRepository;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.PlanAlimenticioRepository;
import com.healthybites.service.AccesoPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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

        // Plan de pago: solo con una suscripción PREMIUM o VIP pagada. Antes, sin suscripción (o sin pagar)
        // se concedía el acceso.
        if (!plan.isEsGratis() && !Suscripcion.isPremiumActiva(suscripcion)) {
            throw new AccessDeniedException("Necesitas una suscripción Premium o VIP pagada para este plan");
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

    @Override
    public boolean isClientePremiumOrVip(Integer clientId) {
        Cliente cliente = clienteRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Antes fallaba con NullPointerException si el usuario no tenía suscripción y no miraba si estaba pagada.
        return Suscripcion.isPremiumActiva(cliente.getUsuario().getSuscripcion());
    }
}

