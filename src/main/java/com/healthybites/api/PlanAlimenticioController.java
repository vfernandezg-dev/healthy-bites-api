package com.healthybites.api;

import com.healthybites.dto.ComidaDiariaDTO;
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

    // Obtiene todos los planes alimenticios
    // http://localhost:8080/plan-alimenticio
    @GetMapping
    public ResponseEntity<List<PlanAlimenticioDTO>> getAll() {
        List<PlanAlimenticioDTO> planAlimenticios = planAlimenticioService.getAll();
        return new ResponseEntity<>(planAlimenticios, HttpStatus.OK);
    }

    // Obtener todas las comidas de un plan alimenticio
    // http://localhost:8080/plan-alimenticio/1/comidas
    @GetMapping("/{id}/comidas")
    public ResponseEntity<List<ComidaDiariaDTO>> getAllComidasByPlanId(@PathVariable Integer id) {
        List<ComidaDiariaDTO> comidasDiarias = planAlimenticioService.getAllComidasByPlanId(id);
        return new ResponseEntity<>(comidasDiarias, HttpStatus.OK);
    }

    // Crear un plan alimenticio
    // http://localhost:8080/plan-alimenticio
    @PostMapping
    public ResponseEntity<PlanAlimenticioDTO> create(@RequestBody PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {
        PlanAlimenticioDTO planAlimenticioDTO = planAlimenticioService.create(planAlimenticioCreateDTO);
        return new ResponseEntity<>(planAlimenticioDTO, HttpStatus.CREATED);
    }

    // Agregar una comida a un plan alimenticio
    // http://localhost:8080/plan-alimenticio/1/comidas
    @PostMapping("/{id}/comidas")
    public ResponseEntity<ComidaDiariaDTO> addComidaToPlan(@PathVariable Integer id, @RequestBody ComidaDiariaDTO comidaDiariaDTO) {
        ComidaDiariaDTO comidaDiariaDetailsDTO = planAlimenticioService.addComidaToPlan(id, comidaDiariaDTO);
        return new ResponseEntity<>(comidaDiariaDetailsDTO, HttpStatus.CREATED);
    }


    // Obtener un plan alimenticio por id
    // http://localhost:8080/plan-alimenticio/1
    @GetMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDTO> getById(@PathVariable Integer id) {
        PlanAlimenticioDTO planAlimenticio = planAlimenticioService.findById(id);
        return new ResponseEntity<>(planAlimenticio, HttpStatus.OK);
    }

    // Obtener una comida de un plan alimenticio por id
    // http://localhost:8080/plan-alimenticio/1/comidas/1
    @GetMapping("/{id}/comidas/{comidaId}")
    public ResponseEntity<ComidaDiariaDTO> getComidaById(@PathVariable Integer id, @PathVariable Integer comidaId) {
        ComidaDiariaDTO comidaDiaria = planAlimenticioService.getComidaById(id, comidaId);
        return new ResponseEntity<>(comidaDiaria, HttpStatus.OK);
    }

    // Actualizar un plan alimenticio
    @PutMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDTO> update(@PathVariable Integer id, @RequestBody PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {
        PlanAlimenticioDTO updatedPlanAlimenticio = planAlimenticioService.update(id, planAlimenticioCreateDTO);
        return new ResponseEntity<>(updatedPlanAlimenticio, HttpStatus.OK);
    }

    // Actualizar una comida de un plan alimenticio
    @PutMapping("/{id}/comidas/{comidaId}")
    public ResponseEntity<ComidaDiariaDTO> updateComida(@PathVariable Integer id,
                                                               @PathVariable Integer comidaId,
                                                               @RequestBody ComidaDiariaDTO comidaDiariaDTO) {
        ComidaDiariaDTO updatedComidaDiaria = planAlimenticioService.updateComida(id, comidaId, comidaDiariaDTO);
        return new ResponseEntity<>(updatedComidaDiaria, HttpStatus.OK);
    }

    // Eliminar un plan alimenticio
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        planAlimenticioService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Eliminar una comida de un plan alimenticio
    @DeleteMapping("/{id}/comidas/{comidaId}")
    public ResponseEntity<Void> deleteComida(@PathVariable Integer id, @PathVariable Integer comidaId) {
        planAlimenticioService.deleteComida(id, comidaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}