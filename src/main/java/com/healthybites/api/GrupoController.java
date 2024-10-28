package com.healthybites.api;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import com.healthybites.service.GrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/grupo")
@PreAuthorize("hasRole('CLIENTE')")
public class GrupoController {

    private final GrupoService grupoService;

    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<GrupoDTO>> getAll(@PathVariable Integer id) {
        List<GrupoDTO> grupos = grupoService.getAll(id);
        return new ResponseEntity<>(grupos, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<GrupoDTO> create(@RequestBody GrupoCreateDTO grupoCreateDTO) {
        GrupoDTO grupoDTO = grupoService.create(grupoCreateDTO);
        return new ResponseEntity<>(grupoDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{grupoId}/cliente/{clienteId}")
    public ResponseEntity<GrupoDTO> getById(@PathVariable Integer clienteId, @PathVariable Integer grupoId) {
        GrupoDTO grupo = grupoService.findByIdAndClienteId(grupoId, clienteId);
        return new ResponseEntity<>(grupo, HttpStatus.OK);
    }

    @PutMapping("/{grupoId}/cliente/{clienteId}")
    public ResponseEntity<GrupoDTO> update(@PathVariable Integer clienteId, @PathVariable Integer grupoId, @RequestBody GrupoCreateDTO updatedGrupoDTO) {
        GrupoDTO grupo = grupoService.update(grupoId, clienteId, updatedGrupoDTO);
        return new ResponseEntity<>(grupo, HttpStatus.OK);
    }

    @DeleteMapping("/{grupoId}/cliente/{clienteId}")
    public ResponseEntity<Void> delete(@PathVariable Integer clienteId, @PathVariable Integer grupoId) {
        grupoService.delete(grupoId, clienteId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
