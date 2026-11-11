package com.healthybites.api;

import com.healthybites.dto.SeguimientoCreateDTO;
import com.healthybites.dto.SeguimientoDTO;
import com.healthybites.service.AdminSeguimientoService;
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
@RequestMapping("/admin/seguimiento")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSeguimientoController {

    private final AdminSeguimientoService adminSeguimientoService;

    @GetMapping
    public ResponseEntity<List<SeguimientoDTO>> getAllSeguimiento() {
        return new ResponseEntity<>(adminSeguimientoService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<SeguimientoDTO>> paginateSeguimientos(
            @PageableDefault(size = 5, sort = "fecha") Pageable pageable) {
        return new ResponseEntity<>(adminSeguimientoService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SeguimientoDTO> getSeguimientoById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminSeguimientoService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<SeguimientoDTO> createSeguimiento(@RequestParam Integer metaId,
                                                            @Valid @RequestBody SeguimientoCreateDTO seguimiento) {
        return new ResponseEntity<>(adminSeguimientoService.create(metaId, seguimiento), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SeguimientoDTO> updateSeguimiento(@PathVariable("id") Integer id,
                                                            @RequestParam(required = false) Integer metaId,
                                                            @Valid @RequestBody SeguimientoCreateDTO seguimiento) {
        return new ResponseEntity<>(adminSeguimientoService.update(id, metaId, seguimiento), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSeguimiento(@PathVariable("id") Integer id) {
        adminSeguimientoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
