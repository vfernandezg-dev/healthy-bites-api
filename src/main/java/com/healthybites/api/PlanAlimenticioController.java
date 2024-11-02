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

    //-----------------------------------------------------------------------------------------------------------


    /*

    // Obtener todas las comidas de un plan alimenticio
    // http://localhost:8080/plan-alimenticio/1/comidas
    @GetMapping("/{id}/comidas")
    public ResponseEntity<List<ComidaDiariaDTO>> getAllComidasByPlanId(@PathVariable Integer id) {
        List<ComidaDiariaDTO> comidasDiarias = planAlimenticioService.getAllComidasByPlanId(id);
        return new ResponseEntity<>(comidasDiarias, HttpStatus.OK);
    }

    // Agregar una comida a un plan alimenticio
    // http://localhost:8080/plan-alimenticio/1/comidas
    @PostMapping("/{id}/comidas")
    public ResponseEntity<ComidaDiariaDTO> addComidaToPlan(@PathVariable Integer id, @RequestBody ComidaDiariaDTO comidaDiariaDTO) {
        ComidaDiariaDTO comidaDiariaDetailsDTO = planAlimenticioService.addComidaToPlan(id, comidaDiariaDTO);
        return new ResponseEntity<>(comidaDiariaDetailsDTO, HttpStatus.CREATED);
    }

    // Obtener una comida de un plan alimenticio por id
    // http://localhost:8080/plan-alimenticio/1/comidas/1
    @GetMapping("/{id}/comidas/{comidaId}")
    public ResponseEntity<ComidaDiariaDTO> getComidaById(@PathVariable Integer id, @PathVariable Integer comidaId) {
        ComidaDiariaDTO comidaDiaria = planAlimenticioService.getComidaById(id, comidaId);
        return new ResponseEntity<>(comidaDiaria, HttpStatus.OK);
    }

    // Actualizar una comida de un plan alimenticio
    @PutMapping("/{id}/comidas/{comidaId}")
    public ResponseEntity<ComidaDiariaDTO> updateComida(@PathVariable Integer id,
                                                        @PathVariable Integer comidaId,
                                                        @RequestBody ComidaDiariaDTO comidaDiariaDTO) {
        ComidaDiariaDTO updatedComidaDiaria = planAlimenticioService.updateComida(id, comidaId, comidaDiariaDTO);
        return new ResponseEntity<>(updatedComidaDiaria, HttpStatus.OK);
    }

    // Eliminar una comida de un plan alimenticio
    @DeleteMapping("/{id}/comidas/{comidaId}")
    public ResponseEntity<Void> deleteComida(@PathVariable Integer id, @PathVariable Integer comidaId) {
        planAlimenticioService.deleteComida(id, comidaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }*/
}