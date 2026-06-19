package com.healthybites.api;

import com.healthybites.model.entity.Suscripcion;
import com.healthybites.service.SuscripcionService;
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
@RequestMapping("/suscripcion")
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @PostMapping
    public ResponseEntity<Suscripcion> createSuscripcion(@RequestBody Suscripcion suscripcions) {
        Suscripcion newSuscripcion = suscripcionService.createSuscripcion(suscripcions);
        return new ResponseEntity<Suscripcion>(newSuscripcion, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Suscripcion>> getSuscripcionHistoryByUserId(@PathVariable("userId") Integer userId) {
        List<Suscripcion> suscripcions = suscripcionService.getSuscripcionHistoryByUserId(userId);
        return new ResponseEntity<List<Suscripcion>>(suscripcions, HttpStatus.OK);
    }

    /*@GetMapping("/page")
    public ResponseEntity<Page<Suscripcion>> paginateSuscripcion(
            @PageableDefault(size = 5, sort = "tipo_suscripcion") Pageable pageable) {
        Page<Suscripcion> suscripcions = suscripcionService.paginate(pageable);
        return new ResponseEntity<Page<Suscripcion>>(suscripcions, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Suscripcion> getSuscripcionById(@PathVariable("id") Integer id) {
        Suscripcion suscripcions = suscripcionService.findById(id);
        return new ResponseEntity<Suscripcion>(suscripcions, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Suscripcion> createSuscripcion(@RequestBody Suscripcion suscripcions) {
        Suscripcion newSuscripcion = suscripcionService.create(suscripcions);
        return new ResponseEntity<Suscripcion>(newSuscripcion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Suscripcion> updateSuscripcion(@PathVariable("id") Integer id, @RequestBody Suscripcion suscripcion) {
        Suscripcion updateSuscripcion = suscripcionService.update(id, suscripcion);
        return new ResponseEntity<Suscripcion>(updateSuscripcion, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Suscripcion> deleteSuscripcion(@PathVariable("id") Integer id) {
        suscripcionService.delete(id);
        return new ResponseEntity<Suscripcion>(HttpStatus.NO_CONTENT);
    }*/
}
