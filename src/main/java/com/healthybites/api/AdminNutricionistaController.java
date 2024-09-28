package com.healthybites.api;

import com.healthybites.model.entity.Nutricionista;
import com.healthybites.service.AdminNutricionistaService;
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
@RequestMapping("/admin/nutricionista")
public class AdminNutricionistaController {

    private final AdminNutricionistaService adminNutricionistaService;

    @GetMapping
    public ResponseEntity<List<Nutricionista>> getAllNutricionista() {
        List<Nutricionista> nutricionistas = adminNutricionistaService.getAll();
        return new ResponseEntity<List<Nutricionista>>(nutricionistas, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Nutricionista>> paginateNutricionistas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<Nutricionista> nutricionistas = adminNutricionistaService.paginate(pageable);
        return new ResponseEntity<Page<Nutricionista>>(nutricionistas, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Nutricionista> getNutricionistaById(@PathVariable("id") Integer id) {
        Nutricionista nutricionista = adminNutricionistaService.findById(id);
        return new ResponseEntity<Nutricionista>(nutricionista, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Nutricionista> createNutricionista(@RequestBody Nutricionista nutricionista) {
        Nutricionista newNutricionista = adminNutricionistaService.create(nutricionista);
        return new ResponseEntity<Nutricionista>(newNutricionista, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Nutricionista> updateNutricionista(@PathVariable("id") Integer id, @RequestBody Nutricionista nutricionista) {
        Nutricionista updateNutricionista = adminNutricionistaService.update(id, nutricionista);
        return new ResponseEntity<Nutricionista>(updateNutricionista, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Nutricionista> deleteNutricionista(@PathVariable("id") Integer id) {
        adminNutricionistaService.delete(id);
        return new ResponseEntity<Nutricionista>(HttpStatus.NO_CONTENT);
    }
}
