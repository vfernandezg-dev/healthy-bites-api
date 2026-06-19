package com.healthybites.api;

import com.healthybites.dto.ComidaDiariaCreateDTO;
import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.service.ComidaDiariaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/plan-alimenticio/{planId}/comidas")
@PreAuthorize("hasAnyRole('ADMIN','NUTRICIONISTA')")
public class ComidaDiariaController {

    private final ComidaDiariaService comidaDiariaService;

    @GetMapping
    public ResponseEntity<List<ComidaDiariaDTO>> getAll(@PathVariable Integer planId) {
        List<ComidaDiariaDTO> comidasDiarias = comidaDiariaService.getAll(planId);
        return new ResponseEntity<>(comidasDiarias, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ComidaDiariaDTO> create(@PathVariable Integer planId, @Valid @RequestBody ComidaDiariaCreateDTO comidaDiariaCreateDTO) {
        ComidaDiariaDTO comidaDiariaDTO = comidaDiariaService.create(planId, comidaDiariaCreateDTO);
        return new ResponseEntity<>(comidaDiariaDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{comidaId}")
    public ResponseEntity<ComidaDiariaDTO> getById(@PathVariable Integer planId, @PathVariable Integer comidaId) {
        ComidaDiariaDTO comidaDiaria = comidaDiariaService.findByIdAndPlanId(comidaId, planId);
        return new ResponseEntity<>(comidaDiaria, HttpStatus.OK);
    }

    @PutMapping("/{comidaId}")
    public ResponseEntity<ComidaDiariaDTO> update(@PathVariable Integer planId,
                                                  @PathVariable Integer comidaId,
                                                  @Valid @RequestBody ComidaDiariaCreateDTO updatedComidaDiariaDTO) {
        ComidaDiariaDTO updatedComidaDiaria = comidaDiariaService.update(planId, comidaId, updatedComidaDiariaDTO);
        return new ResponseEntity<>(updatedComidaDiaria, HttpStatus.OK);
    }

    @DeleteMapping("/{comidaId}")
    public ResponseEntity<Void> delete(@PathVariable Integer planId, @PathVariable Integer comidaId) {
        comidaDiariaService.delete(planId, comidaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
