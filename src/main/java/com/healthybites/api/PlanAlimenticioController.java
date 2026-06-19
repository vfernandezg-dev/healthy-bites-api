package com.healthybites.api;

import com.healthybites.dto.PlanAlimenticioCreateDTO;
import com.healthybites.dto.PlanAlimenticioDTO;
import com.healthybites.service.PlanAlimenticioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/plan-alimenticio")
public class PlanAlimenticioController {

    private final PlanAlimenticioService planAlimenticioService;

    @GetMapping
    public ResponseEntity<List<PlanAlimenticioDTO>> getAll() {
        List<PlanAlimenticioDTO> planAlimenticios = planAlimenticioService.getAll();
        return new ResponseEntity<>(planAlimenticios, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PlanAlimenticioDTO> create(@RequestBody PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {
        PlanAlimenticioDTO planAlimenticioDTO = planAlimenticioService.create(planAlimenticioCreateDTO);
        return new ResponseEntity<>(planAlimenticioDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDTO> getById(@PathVariable Integer id) {
        PlanAlimenticioDTO planAlimenticio = planAlimenticioService.findById(id);
        return new ResponseEntity<>(planAlimenticio, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDTO> update(@PathVariable Integer id, @RequestBody PlanAlimenticioCreateDTO planAlimenticioCreateDTO) {
        PlanAlimenticioDTO updatedPlanAlimenticio = planAlimenticioService.update(id, planAlimenticioCreateDTO);
        return new ResponseEntity<>(updatedPlanAlimenticio, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        planAlimenticioService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}