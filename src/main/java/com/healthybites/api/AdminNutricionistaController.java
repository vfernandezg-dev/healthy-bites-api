package com.healthybites.api;

import com.healthybites.dto.NutricionistaDTO;
import com.healthybites.model.entity.Nutricionista;
import com.healthybites.service.AdminNutricionistaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/nutricionista")
public class AdminNutricionistaController {

    @Autowired
    private final AdminNutricionistaService adminNutricionistaService;

    @GetMapping
    public ResponseEntity<List<NutricionistaDTO>> getAllNutricionista() {
        List<NutricionistaDTO> nutricionistas = adminNutricionistaService.getAll();
        return new ResponseEntity<>(nutricionistas, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<NutricionistaDTO>> paginateNutricionistas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<NutricionistaDTO> nutricionista = adminNutricionistaService.paginate(pageable);
        return new ResponseEntity<>(nutricionista, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NutricionistaDTO> getNutricionistaById(@PathVariable("id") Integer id) {
        NutricionistaDTO nutricionista = adminNutricionistaService.findById(id);
        return new ResponseEntity<>(nutricionista, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<NutricionistaDTO> createNutricionista(@Valid @RequestBody NutricionistaDTO nutricionistaDTO) {
        NutricionistaDTO newNutricionista = adminNutricionistaService.create(nutricionistaDTO);
        return new ResponseEntity<>(newNutricionista, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NutricionistaDTO> updateNutricionista(@PathVariable("id") Integer id,@Valid @RequestBody NutricionistaDTO nutricionistaDTO) {
        NutricionistaDTO updateNutricionista = adminNutricionistaService.update(id, nutricionistaDTO);
        return new ResponseEntity<>(updateNutricionista, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Nutricionista> deleteNutricionista(@PathVariable("id") Integer id) {
        adminNutricionistaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
