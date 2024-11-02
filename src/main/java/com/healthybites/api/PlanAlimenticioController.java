package com.healthybites.api;

import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.service.PlanAlimenticioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/plan-alimenticio")
@PreAuthorize("hasAnyRole('ADMIN','NUTRICIONISTA')")
public class PlanAlimenticioController {

    private final PlanAlimenticioService planAlimenticioService;

    @GetMapping("/nutricionista/{id}")
    public ResponseEntity<List<PlanAlimenticioDTO>> getAll(@PathVariable Integer id) {
        List<PlanAlimenticioDTO> planAlimenticios = planAlimenticioService.getAll(id);
        return new ResponseEntity<>(planAlimenticios, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PlanAlimenticioDTO> create(@RequestBody PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {
        PlanAlimenticioDTO planAlimenticioDTO = planAlimenticioService.create(planAlimenticioCreateDTO);
        return new ResponseEntity<>(planAlimenticioDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{planId}/nutricionista/{nutricionistaId}")
    public ResponseEntity<PlanAlimenticioDTO> getById(@PathVariable Integer nutricionistaId, @PathVariable Integer planId) {
        PlanAlimenticioDTO planAlimenticio = planAlimenticioService.findByIdAndNutricionistaId(planId, nutricionistaId);
        return new ResponseEntity<>(planAlimenticio, HttpStatus.OK);
    }

    @PutMapping("/{planId}/nutricionista/{nutricionistaId}")
    public ResponseEntity<PlanAlimenticioDTO> update(@PathVariable Integer nutricionistaId,
                                                     @PathVariable Integer planId,
                                                     @RequestBody PlanAlimenticioCreateDTO updatedPlanAlimenticioDTO) {
        PlanAlimenticioDTO updatedPlanAlimenticio = planAlimenticioService.update(planId, nutricionistaId, updatedPlanAlimenticioDTO);
        return new ResponseEntity<>(updatedPlanAlimenticio, HttpStatus.OK);
    }

    @DeleteMapping("/{planId}/nutricionista/{nutricionistaId}")
    public ResponseEntity<Void> delete(@PathVariable Integer nutricionistaId, @PathVariable Integer planId) {
        planAlimenticioService.delete(planId, nutricionistaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}