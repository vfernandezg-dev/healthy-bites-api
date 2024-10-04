package com.healthybites.api;

import com.healthybites.model.entity.ComidaDiaria;
import com.healthybites.service.AdminComidaDiariaService;
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
@RequestMapping("/admin/comida-diaria")
public class AdminComidaDiariaController {

    private final AdminComidaDiariaService adminComidaDiariaService;

    @GetMapping
    public ResponseEntity<List<ComidaDiaria>> getAllComidaDiaria() {
        List<ComidaDiaria> comidasDiarias = adminComidaDiariaService.getAll();
        return new ResponseEntity<List<ComidaDiaria>>(comidasDiarias, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ComidaDiaria>> paginateComidasDiarias(
            @PageableDefault(size = 5, sort = "nombre_comida") Pageable pageable) {
        Page<ComidaDiaria> comidasDiarias = adminComidaDiariaService.paginate(pageable);
        return new ResponseEntity<Page<ComidaDiaria>>(comidasDiarias, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComidaDiaria> getComidaDiariaById(@PathVariable("id") Integer id) {
        ComidaDiaria comidaDiaria = adminComidaDiariaService.findById(id);
        return new ResponseEntity<ComidaDiaria>(comidaDiaria, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ComidaDiaria> createComidaDiaria(@RequestBody ComidaDiaria comidaDiaria) {
        ComidaDiaria newComidaDiaria = adminComidaDiariaService.create(comidaDiaria);
        return new ResponseEntity<ComidaDiaria>(newComidaDiaria, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComidaDiaria> updateComidaDiaria(@PathVariable("id") Integer id, @RequestBody ComidaDiaria comidaDiaria) {
        ComidaDiaria updateComidaDiaria = adminComidaDiariaService.update(id, comidaDiaria);
        return new ResponseEntity<ComidaDiaria>(updateComidaDiaria, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ComidaDiaria> deleteComidaDiaria(@PathVariable("id") Integer id) {
        adminComidaDiariaService.delete(id);
        return new ResponseEntity<ComidaDiaria>(HttpStatus.NO_CONTENT);
    }
}
