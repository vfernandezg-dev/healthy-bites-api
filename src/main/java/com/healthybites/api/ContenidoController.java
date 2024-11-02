package com.healthybites.api;

import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.ContenidoDetailsDTO;
import com.healthybites.model.entity.Contenido;
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
@RequestMapping("/admin/contenido")
@PreAuthorize("hasAnyRole('NUTRICIONISTA','ADMIN')")
public class AdminContenidoController {

    private final ContenidoService contenidoService;

    @GetMapping
    public ResponseEntity<List<ContenidoDetailsDTO>> list() {
        List<ContenidoDetailsDTO> contenidos = contenidoService.getAll();
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ContenidoDetailsDTO> create(@Valid @RequestBody ContenidoCreateUpdateDTO contenidoFormDTO) {
        ContenidoDetailsDTO contenidoDetailsDTO = contenidoService.create(contenidoFormDTO);
        return new ResponseEntity<>(contenidoDetailsDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenidoDetailsDTO> get(@PathVariable Integer id) {
        ContenidoDetailsDTO contenido = contenidoService.findById(id);
        return new ResponseEntity<>(contenido, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContenidoDetailsDTO> update(@PathVariable Integer id, @Valid @RequestBody ContenidoCreateUpdateDTO contenidoFormDTO) {
        ContenidoDetailsDTO updatedContenido = contenidoService.update(id, contenidoFormDTO);
        return new ResponseEntity<>(updatedContenido, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        contenidoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
