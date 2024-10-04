package com.healthybites.api;

import com.healthybites.model.entity.Seguimiento;
import com.healthybites.service.AdminSeguimientoService;
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
@RequestMapping("/admin/seguimiento")
public class AdminSeguimientoController {

    private final AdminSeguimientoService adminSeguimientoService;

    @GetMapping
    public ResponseEntity<List<Seguimiento>> getAllSeguimiento() {
        List<Seguimiento> seguimientos = adminSeguimientoService.getAll();
        return new ResponseEntity<List<Seguimiento>>(seguimientos, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Seguimiento>> paginateSeguimientos(
            @PageableDefault(size = 5, sort = "fecha") Pageable pageable) {
        Page<Seguimiento> seguimientos = adminSeguimientoService.paginate(pageable);
        return new ResponseEntity<Page<Seguimiento>>(seguimientos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Seguimiento> getSeguimientoById(@PathVariable("id") Integer id) {
        Seguimiento seguimiento = adminSeguimientoService.findById(id);
        return new ResponseEntity<Seguimiento>(seguimiento, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Seguimiento> createSeguimiento(@RequestBody Seguimiento seguimiento) {
        Seguimiento newSeguimiento = adminSeguimientoService.create(seguimiento);
        return new ResponseEntity<Seguimiento>(newSeguimiento, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Seguimiento> updateSeguimiento(@PathVariable("id") Integer id, @RequestBody Seguimiento seguimiento) {
        Seguimiento updateSeguimiento = adminSeguimientoService.update(id, seguimiento);
        return new ResponseEntity<Seguimiento>(updateSeguimiento, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Seguimiento> deleteSeguimiento(@PathVariable("id") Integer id) {
        adminSeguimientoService.delete(id);
        return new ResponseEntity<Seguimiento>(HttpStatus.NO_CONTENT);
    }
}
