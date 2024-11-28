package com.healthybites.api;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;
import com.healthybites.service.SeguimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/meta/{metaId}/seguimientos")
@PreAuthorize("hasRole('CLIENTE')")
public class SeguimientoController {

    private final SeguimientoService seguimientoService;

    @GetMapping
    public ResponseEntity<List<SeguimientoDTO>> getAll(@PathVariable Integer metaId) {
        List<SeguimientoDTO> seguimientos = seguimientoService.getAll(metaId);
        return new ResponseEntity<>(seguimientos, HttpStatus.OK);
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<SeguimientoDTO>> getAllByClienteId(@PathVariable Integer metaId, @PathVariable Integer clienteId) {
        List<SeguimientoDTO> seguimientos = seguimientoService.getAllSeguimientosByMetaIdAndClienteId(metaId, clienteId);
        return new ResponseEntity<>(seguimientos, HttpStatus.OK);
    }

    @GetMapping("/cliente/{clienteId}/page")
    public ResponseEntity<Page<SeguimientoDTO>> paginateByClienteId(
            @PathVariable Integer metaId,
            @PathVariable Integer clienteId,
            @PageableDefault(size = 5, sort = "fecha") Pageable pageable) {
        Page<SeguimientoDTO> page = seguimientoService.paginateSeguimientosByMetaIdAndClienteId(metaId, clienteId, pageable);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<SeguimientoDTO>>paginate(
            @PageableDefault(size = 5, sort = "fecha") Pageable pageable) {
        Page<SeguimientoDTO> page = seguimientoService.paginate(pageable);
        return new ResponseEntity<>(page, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SeguimientoDTO> create(@PathVariable Integer metaId, @Valid @RequestBody SeguimientoCreateDTO seguimientoCreateDTO) {
        SeguimientoDTO seguimientoDTO = seguimientoService.create(metaId, seguimientoCreateDTO);
        return new ResponseEntity<>(seguimientoDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{seguimientoId}")
    public ResponseEntity<SeguimientoDTO> getById(@PathVariable Integer metaId, @PathVariable Integer seguimientoId) {
        SeguimientoDTO seguimiento = seguimientoService.findByIdAndMetaId(seguimientoId, metaId);
        return new ResponseEntity<>(seguimiento, HttpStatus.OK);
    }

    @PutMapping("/{seguimientoId}")
    public ResponseEntity<SeguimientoDTO> update(@PathVariable Integer metaId,
                                                  @PathVariable Integer seguimientoId,
                                                  @Valid @RequestBody SeguimientoCreateDTO updatedSeguimientoDTO) {
        SeguimientoDTO updatedSeguimiento = seguimientoService.update(metaId, seguimientoId, updatedSeguimientoDTO);
        return new ResponseEntity<>(updatedSeguimiento, HttpStatus.OK);
    }

    @DeleteMapping("/{seguimientoId}")
    public ResponseEntity<Void> delete(@PathVariable Integer metaId, @PathVariable Integer seguimientoId) {
        seguimientoService.delete(metaId, seguimientoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
