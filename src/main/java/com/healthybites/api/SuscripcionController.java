package com.healthybites.api;

import com.healthybites.dto.SuscripcionCreateUpdateDTO;
import com.healthybites.dto.SuscripcionDetailsDTO;
import com.healthybites.service.SuscripcionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/suscripcion")
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")  // Restrict access to authenticated users
public class SuscripcionController {

    private final SuscripcionService suscripcionService;

    @GetMapping
    public ResponseEntity<List<SuscripcionDetailsDTO>> list() {
        List<SuscripcionDetailsDTO> suscripciones = suscripcionService.getAll();
        return new ResponseEntity<>(suscripciones, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SuscripcionDetailsDTO> create(@Valid @RequestBody SuscripcionCreateUpdateDTO suscripcionFormDTO) {
        SuscripcionDetailsDTO suscripcionDetailsDTO = suscripcionService.create(suscripcionFormDTO);
        return new ResponseEntity<>(suscripcionDetailsDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SuscripcionDetailsDTO> get(@PathVariable Integer id) {
        SuscripcionDetailsDTO suscripcion = suscripcionService.findById(id);
        return new ResponseEntity<>(suscripcion, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        suscripcionService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping("/confirm/{id}")
    public ResponseEntity<SuscripcionDetailsDTO> confirmPurchase(@PathVariable Integer id) {
        SuscripcionDetailsDTO confirmedPurchase = suscripcionService.confirmSuscripcion(id);
        return ResponseEntity.ok(confirmedPurchase);
    }
}
