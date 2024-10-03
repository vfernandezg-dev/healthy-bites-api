package com.healthybites.api;

import com.healthybites.dto.PlanAlimenticioCreateUpdateDTO;
import com.healthybites.dto.PlanAlimenticioDetailsDTO;
import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.service.AdminPlanAlimenticioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/plan-alimenticio")
public class AdminPlanAlimenticioController {

    private final AdminPlanAlimenticioService adminPlanAlimenticioService;

    @GetMapping
    public ResponseEntity<List<PlanAlimenticioDetailsDTO>> getAllPlanAlimenticio() {
        List<PlanAlimenticioDetailsDTO> planAlimenticios = adminPlanAlimenticioService.getAll();
        return new ResponseEntity<>(planAlimenticios, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<PlanAlimenticioDetailsDTO>> paginatePlanAlimenticios(
            @PageableDefault(size = 5, sort = "plan_objetivo") Pageable pageable) {
        Page<PlanAlimenticioDetailsDTO> planAlimenticios = adminPlanAlimenticioService.paginate(pageable);
        return new ResponseEntity<>(planAlimenticios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDetailsDTO> getPlanAlimenticioById(@PathVariable("id") Integer id) {
        PlanAlimenticioDetailsDTO planAlimenticio = adminPlanAlimenticioService.findById(id);
        return new ResponseEntity<>(planAlimenticio, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PlanAlimenticioDetailsDTO> createPlanAlimenticio(@Valid @RequestBody PlanAlimenticioCreateUpdateDTO planAlimenticio) {
        PlanAlimenticioDetailsDTO newPlanAlimenticio = adminPlanAlimenticioService.create(planAlimenticio);
        return new ResponseEntity<>(newPlanAlimenticio, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDetailsDTO> updatePlanAlimenticio(@PathVariable("id") Integer id,@Valid @RequestBody PlanAlimenticioCreateUpdateDTO planAlimenticio) {
        PlanAlimenticioDetailsDTO updatePlanAlimenticio = adminPlanAlimenticioService.update(id, planAlimenticio);
        return new ResponseEntity<>(updatePlanAlimenticio, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PlanAlimenticio> deletePlanAlimenticio(@PathVariable("id") Integer id) {
        adminPlanAlimenticioService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
