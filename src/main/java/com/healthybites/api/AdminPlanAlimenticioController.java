package com.healthybites.api;

import com.healthybites.model.entity.PlanAlimenticio;
import com.healthybites.service.AdminPlanAlimenticioService;
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
    public ResponseEntity<List<PlanAlimenticio>> getAllPlanAlimenticio() {
        List<PlanAlimenticio> planAlimenticios = adminPlanAlimenticioService.getAll();
        return new ResponseEntity<List<PlanAlimenticio>>(planAlimenticios, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<PlanAlimenticio>> paginatePlanAlimenticios(
            @PageableDefault(size = 5, sort = "plan_objetivo") Pageable pageable) {
        Page<PlanAlimenticio> planAlimenticios = adminPlanAlimenticioService.paginate(pageable);
        return new ResponseEntity<Page<PlanAlimenticio>>(planAlimenticios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanAlimenticio> getPlanAlimenticioById(@PathVariable("id") Integer id) {
        PlanAlimenticio planAlimenticio = adminPlanAlimenticioService.findById(id);
        return new ResponseEntity<PlanAlimenticio>(planAlimenticio, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PlanAlimenticio> createPlanAlimenticio(@RequestBody PlanAlimenticio planAlimenticio) {
        PlanAlimenticio newPlanAlimenticio = adminPlanAlimenticioService.create(planAlimenticio);
        return new ResponseEntity<PlanAlimenticio>(newPlanAlimenticio, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanAlimenticio> updatePlanAlimenticio(@PathVariable("id") Integer id, @RequestBody PlanAlimenticio planAlimenticio) {
        PlanAlimenticio updatePlanAlimenticio = adminPlanAlimenticioService.update(id, planAlimenticio);
        return new ResponseEntity<PlanAlimenticio>(updatePlanAlimenticio, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<PlanAlimenticio> deletePlanAlimenticio(@PathVariable("id") Integer id) {
        adminPlanAlimenticioService.delete(id);
        return new ResponseEntity<PlanAlimenticio>(HttpStatus.NO_CONTENT);
    }
}
