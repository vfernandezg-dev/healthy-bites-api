package com.healthybites.api;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.service.MetaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/meta")
@PreAuthorize("hasRole('CLIENTE')")
public class MetaController {

    private final MetaService metaService;

    @GetMapping("/cliente/{id}")
    public ResponseEntity<List<MetaDTO>> getAll(@PathVariable Integer id) {
        List<MetaDTO> metas = metaService.getAll(id);
        return new ResponseEntity<>(metas, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<MetaDTO> create(@RequestBody MetaCreateDTO metaCreateDTO) {
        MetaDTO createdMeta = metaService.create(metaCreateDTO);
        return new ResponseEntity<>(createdMeta, HttpStatus.CREATED);
    }

    @GetMapping("/{metaId}/cliente/{clienteId}")
    public ResponseEntity<MetaDTO> getById(@PathVariable Integer clienteId, @PathVariable Integer metaId) {
        MetaDTO meta = metaService.findByIdAndClienteId(metaId, clienteId);
        return new ResponseEntity<>(meta, HttpStatus.OK);
    }

    @PutMapping("/{metaId}/cliente/{clienteId}")
    public ResponseEntity<MetaDTO> update(@PathVariable Integer clienteId,
                                          @PathVariable Integer metaId,
                                          @RequestBody MetaCreateDTO updatedMetaDTO) {
        MetaDTO updatedMeta = metaService.update(metaId, clienteId, updatedMetaDTO);
        return new ResponseEntity<>(updatedMeta, HttpStatus.OK);
    }

    @DeleteMapping("/{metaId}/cliente/{clienteId}")
    public ResponseEntity<Void> delete(@PathVariable Integer clienteId, @PathVariable Integer metaId) {
        metaService.delete(metaId, clienteId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
