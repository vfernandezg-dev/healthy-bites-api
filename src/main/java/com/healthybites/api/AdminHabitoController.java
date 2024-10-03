package com.healthybites.api;

import com.healthybites.model.entity.Habito;
import com.healthybites.service.AdminHabitoService;
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
@RequestMapping("/admin/habito")
public class AdminHabitoController {

    private final AdminHabitoService adminHabitoService;

    @GetMapping
    public ResponseEntity<List<Habito>> getAllHabito() {
        List<Habito> habitos = adminHabitoService.getAll();
        return new ResponseEntity<List<Habito>>(habitos, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Habito>> paginateHabitos(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<Habito> habitos = adminHabitoService.paginate(pageable);
        return new ResponseEntity<Page<Habito>>(habitos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Habito> getHabitoById(@PathVariable("id") Integer id) {
        Habito habito = adminHabitoService.findById(id);
        return new ResponseEntity<Habito>(habito, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Habito> createHabito(@RequestBody Habito habito) {
        Habito newHabito = adminHabitoService.create(habito);
        return new ResponseEntity<Habito>(newHabito, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Habito> updateHabito(@PathVariable("id") Integer id, @RequestBody Habito habito) {
        Habito updateHabito = adminHabitoService.update(id, habito);
        return new ResponseEntity<Habito>(updateHabito, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Habito> deleteHabito(@PathVariable("id") Integer id) {
        adminHabitoService.delete(id);
        return new ResponseEntity<Habito>(HttpStatus.NO_CONTENT);
    }
}
