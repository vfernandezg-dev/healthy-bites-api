package com.healthybites.api;

import com.healthybites.dto.PublicacionCreateDTO;
import com.healthybites.dto.PublicacionDTO;
import com.healthybites.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/publicacion")
@PreAuthorize("hasRole('CLIENTE')")
public class PublicacionController {

    private final PublicacionService publicacionService;

    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<PublicacionDTO>> getAll(@PathVariable Integer id) {
        List<PublicacionDTO> publicaciones = publicacionService.getAll(id);
        return new ResponseEntity<>(publicaciones, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PublicacionDTO> create(@Valid @RequestBody PublicacionCreateDTO publicacionCreateDTO) {
        PublicacionDTO publicacionDTO = publicacionService.create(publicacionCreateDTO);
        return new ResponseEntity<>(publicacionDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{publicacionId}/cliente/{clienteId}")
    public ResponseEntity<PublicacionDTO> getById(@PathVariable Integer clienteId, @PathVariable Integer publicacionId) {
        PublicacionDTO publicacion = publicacionService.findByIdAndClienteId(publicacionId, clienteId);
        return new ResponseEntity<>(publicacion, HttpStatus.OK);
    }

    @PutMapping("/{publicacionId}/cliente/{clienteId}")
    public ResponseEntity<PublicacionDTO> update(@PathVariable Integer clienteId, @PathVariable Integer publicacionId, @Valid @RequestBody PublicacionCreateDTO updatedPublicacionDTO) {
        PublicacionDTO publicacion = publicacionService.update(publicacionId, clienteId, updatedPublicacionDTO);
        return new ResponseEntity<>(publicacion, HttpStatus.OK);
    }

    @DeleteMapping("/{publicacionId}/cliente/{clienteId}")
    public ResponseEntity<Void> delete(@PathVariable Integer clienteId, @PathVariable Integer publicacionId) {
        publicacionService.delete(publicacionId, clienteId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
