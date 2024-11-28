package com.healthybites.api;

import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.service.AccesoPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/acceso-plan")
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
public class AccesoPlanController {

    private final AccesoPlanService accesoPlanService;

    // Metodo para agregar un plan a un cliente
    //http://localhost:8080/acceso-plan/1/add-plan?planId=1
    @PostMapping("/{clientId}/add-plan")
    public ResponseEntity<PlanAlimenticioDTO> addPlanToClient(@PathVariable Integer clientId,
                                                              @RequestParam Integer planId) {
        PlanAlimenticioDTO planAlimenticioDTO = accesoPlanService.addPlanToClient(clientId, planId);
        return new ResponseEntity<>(planAlimenticioDTO, HttpStatus.CREATED);
    }

    // Metodo para eliminar un plan de un cliente
    //http://localhost:8080/acceso-plan/1/remove-plan/1
    @DeleteMapping("/{clientId}/remove-plan/{planId}")
    public ResponseEntity<Void> removePlanFromClient(@PathVariable Integer clientId,
                                                     @PathVariable Integer planId) {
        accesoPlanService.removePlanFromClient(clientId, planId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Metodo para obtener todos los planes de un cliente
    //http://localhost:8080/acceso-plan/1/all-planes
    @GetMapping("/{clientId}/all-planes")
    public ResponseEntity<List<PlanAlimenticioDTO>> getAllPlansByClient(@PathVariable Integer clientId) {
        List<PlanAlimenticioDTO> planes = accesoPlanService.getAllPlansByClient(clientId);
        return new ResponseEntity<>(planes, HttpStatus.OK);
    }

    @GetMapping("/{clientId}/is-premium-or-vip")
    public ResponseEntity<Boolean> isClientePremiumOrVip(@PathVariable Integer clientId) {
        boolean isPremiumOrVip = accesoPlanService.isClientePremiumOrVip(clientId);
        return new ResponseEntity<>(isPremiumOrVip, HttpStatus.OK);
    }
}
