package com.healthybites.api;

import com.healthybites.model.entity.AccesoPlan;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.service.AccesoPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/acceso-plan")
public class AccesoPlanController {

    private final AccesoPlanService accesoPlanService;

    @PostMapping("/{clienteId}/add-plan-alimenticio")
    public ResponseEntity<AccesoPlan> addPlanToCliente(@PathVariable Integer clienteId, @RequestParam Integer planId) {
        AccesoPlan accesoPlan = accesoPlanService.addPlanToCliente(clienteId, planId);
        return new ResponseEntity<>(accesoPlan, HttpStatus.CREATED);
    }

    @DeleteMapping("/{clienteId}/remove-plan/{planId}")
    public ResponseEntity<Void> removePlanFromCliente(@PathVariable Integer clienteId, @PathVariable Integer planId) {
        accesoPlanService.removePlanFromCliente(clienteId, planId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{clienteId}/plan")
    public ResponseEntity<List<PlanAlimenticio>> getPlanesInCliente(@PathVariable Integer clienteId) {
        List<PlanAlimenticio> planes = accesoPlanService.getPlanesInCliente(clienteId);
        return new ResponseEntity<>(planes, HttpStatus.OK);
    }
}
