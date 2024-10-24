package com.healthybites.api;

import com.healthybites.dto.MetaCreateDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.dto.SeguimientoCreateUpdateDTO;
import com.healthybites.dto.SeguimientoDetailsDTO;
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
public class MetaController {

    private final MetaService metaService;

    // Obtener todas las Metas
    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','NUTRICIONISTA')")
    public ResponseEntity<List<MetaDTO>> getAll() {
        List<MetaDTO> metas = metaService.getAll();
        return new ResponseEntity<>(metas, HttpStatus.OK);
    }

    // Obtener todos los Seguimientos de una Meta
    @GetMapping("/{metaId}/seguimiento")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public ResponseEntity<List<SeguimientoDetailsDTO>> getAllSeguimientosByMetaId(@PathVariable Integer metaId) {
        List<SeguimientoDetailsDTO> seguimientos = metaService.getAllSeguimientosByMetaId(metaId);
        return new ResponseEntity<>(seguimientos, HttpStatus.OK);
    }

    // Crear una Meta
    @PostMapping
    @PreAuthorize("hasAnyRole('NUTRICIONISTA','ADMIN')")
    public ResponseEntity<MetaDTO> create(@RequestBody MetaCreateDTO metaCreateDTO) {
        MetaDTO createdMeta = metaService.create(metaCreateDTO);
        return new ResponseEntity<>(createdMeta, HttpStatus.CREATED);
    }

    // Agregar un Seguimiento a una Meta
    @PostMapping("/{metaId}/seguimiento")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public ResponseEntity<SeguimientoDetailsDTO> addSeguimiento(@PathVariable Integer metaId, @RequestBody SeguimientoCreateUpdateDTO seguimientoCreateDTO) {
        SeguimientoDetailsDTO seguimiento = metaService.addSeguimientoToMeta(metaId, seguimientoCreateDTO);
        return new ResponseEntity<>(seguimiento, HttpStatus.CREATED);
    }

    // Obtener una Meta por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public ResponseEntity<MetaDTO> getById(@PathVariable Integer id) {
        MetaDTO meta = metaService.findById(id);
        return new ResponseEntity<>(meta, HttpStatus.OK);
    }

    // Obtener un Seguimiento específico de una Meta
    @GetMapping("/{metaId}/seguimiento/{seguimientoId}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public ResponseEntity<SeguimientoDetailsDTO> getSeguimientoById(
            @PathVariable Integer metaId,
            @PathVariable Integer seguimientoId) {

        SeguimientoDetailsDTO seguimientoDetails = metaService.getSeguimientoById(metaId, seguimientoId);
        return new ResponseEntity<>(seguimientoDetails, HttpStatus.OK);
    }

    // Actualizar una Meta
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('NUTRICIONISTA','ADMIN')")
    public ResponseEntity<MetaDTO> update(@PathVariable Integer id, @RequestBody MetaCreateDTO metaCreateDTO) {
        MetaDTO updatedMeta = metaService.update(id, metaCreateDTO);
        return new ResponseEntity<>(updatedMeta, HttpStatus.OK);
    }

    // Actualizar un Seguimiento específico de una Meta
    @PutMapping("/{metaId}/seguimiento/{seguimientoId}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public ResponseEntity<SeguimientoDetailsDTO> updateSeguimiento(@PathVariable Integer metaId,
                                                                   @PathVariable Integer seguimientoId,
                                                                   @RequestBody SeguimientoCreateUpdateDTO seguimientoUpdateDTO) {
        SeguimientoDetailsDTO updatedSeguimiento = metaService.updateSeguimiento(metaId, seguimientoId, seguimientoUpdateDTO);
        return new ResponseEntity<>(updatedSeguimiento, HttpStatus.OK);
    }

    // Eliminar una Meta y todos sus Seguimientos
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteMeta(@PathVariable Integer id) {
        metaService.delete(id); // Esto elimina la meta y todos sus seguimientos
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Eliminar un Seguimiento específico de una Meta
    @DeleteMapping("/{metaId}/seguimiento/{seguimientoId}")
    @PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
    public ResponseEntity<Void> deleteSeguimientoFromMeta(@PathVariable Integer metaId, @PathVariable Integer seguimientoId) {
        metaService.deleteSeguimiento(metaId, seguimientoId); // Esto elimina un seguimiento específico de una meta
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
