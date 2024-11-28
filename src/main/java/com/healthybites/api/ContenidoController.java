package com.healthybites.api;

import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.ContenidoDTO;
import com.healthybites.service.ContenidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/contenido")
@PreAuthorize("hasAnyRole('NUTRICIONISTA','CLIENTE')")
public class ContenidoController {

    private final ContenidoService contenidoService;

    @GetMapping
    public ResponseEntity<List<ContenidoDTO>> list() {
        List<ContenidoDTO> contenidos = contenidoService.getAll();
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

    @GetMapping("/nutricionista/{nutricionistaId}")
    public ResponseEntity<List<ContenidoDTO>> listByNutricionistaId(@PathVariable Integer nutricionistaId) {
        List<ContenidoDTO> contenidos = contenidoService.getAllByNutricionistaId(nutricionistaId);
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ContenidoDTO> create(@Valid @RequestBody ContenidoCreateUpdateDTO contenidoFormDTO) {
        ContenidoDTO contenidoDetailsDTO = contenidoService.create(contenidoFormDTO);
        return new ResponseEntity<>(contenidoDetailsDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenidoDTO> get(@PathVariable Integer id) {
        ContenidoDTO contenido = contenidoService.findById(id);
        return new ResponseEntity<>(contenido, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContenidoDTO> update(@PathVariable Integer id, @Valid @RequestBody ContenidoCreateUpdateDTO contenidoFormDTO) {
        ContenidoDTO updatedContenido = contenidoService.update(id, contenidoFormDTO);
        return new ResponseEntity<>(updatedContenido, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        contenidoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}