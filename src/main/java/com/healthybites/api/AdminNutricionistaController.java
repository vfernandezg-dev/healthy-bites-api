package com.healthybites.api;

import com.healthybites.dto.NutricionistaDTO;
import com.healthybites.service.AdminNutricionistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/nutricionista")
@PreAuthorize("hasRole('ADMIN')")
public class AdminNutricionistaController {

    private final AdminNutricionistaService adminNutricionistaService;

    @GetMapping
    public ResponseEntity<List<NutricionistaDTO>> getAllNutricionista() {
        List<NutricionistaDTO> nutricionistas = adminNutricionistaService.getAll();
        return new ResponseEntity<>(nutricionistas, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<NutricionistaDTO> createNutricionista(@Valid @RequestBody NutricionistaDTO nutricionistaDTO) {
        NutricionistaDTO newNutricionista = adminNutricionistaService.create(nutricionistaDTO);
        return new ResponseEntity<>(newNutricionista, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NutricionistaDTO> getNutricionistaById(@PathVariable Integer id) {
        NutricionistaDTO nutricionista = adminNutricionistaService.findById(id);
        return new ResponseEntity<>(nutricionista, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NutricionistaDTO> updateNutricionista(@PathVariable Integer id, @Valid @RequestBody NutricionistaDTO nutricionistaDTO) {
        NutricionistaDTO updatedNutricionista = adminNutricionistaService.update(id, nutricionistaDTO);
        return new ResponseEntity<>(updatedNutricionista, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNutricionista(@PathVariable Integer id) {
        adminNutricionistaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
