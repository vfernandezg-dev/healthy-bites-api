package com.healthybites.api;

import com.healthybites.dto.PlanAlimenticioCreateUpdateDTO;
import com.healthybites.dto.PlanAlimenticioDetailsDTO;
import com.healthybites.service.AdminPlanAlimenticioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/plan-alimenticio")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPlanAlimenticioController {

    private final AdminPlanAlimenticioService adminPlanAlimenticioService;

    @GetMapping
    public ResponseEntity<List<PlanAlimenticioDetailsDTO>> getAllPlanAlimenticio() {
        return new ResponseEntity<>(adminPlanAlimenticioService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<PlanAlimenticioDetailsDTO>> paginatePlanAlimenticios(
            @PageableDefault(size = 5, sort = "planObjetivo") Pageable pageable) {
        return new ResponseEntity<>(adminPlanAlimenticioService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDetailsDTO> getPlanAlimenticioById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminPlanAlimenticioService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PlanAlimenticioDetailsDTO> createPlanAlimenticio(
            @Valid @RequestBody PlanAlimenticioCreateUpdateDTO planAlimenticio) {
        return new ResponseEntity<>(adminPlanAlimenticioService.create(planAlimenticio), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PlanAlimenticioDetailsDTO> updatePlanAlimenticio(
            @PathVariable("id") Integer id,
            @Valid @RequestBody PlanAlimenticioCreateUpdateDTO planAlimenticio) {
        return new ResponseEntity<>(adminPlanAlimenticioService.update(id, planAlimenticio), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlanAlimenticio(@PathVariable("id") Integer id) {
        adminPlanAlimenticioService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
