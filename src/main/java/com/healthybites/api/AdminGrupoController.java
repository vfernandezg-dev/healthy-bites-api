package com.healthybites.api;

import com.healthybites.dto.GrupoCreateDTO;
import com.healthybites.dto.GrupoDTO;
import com.healthybites.service.AdminGrupoService;
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
@RequestMapping("/admin/grupo")
@PreAuthorize("hasRole('ADMIN')")
public class AdminGrupoController {

    private final AdminGrupoService adminGrupoService;

    @GetMapping
    public ResponseEntity<List<GrupoDTO>> getAllGrupo() {
        return new ResponseEntity<>(adminGrupoService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<GrupoDTO>> paginateGrupos(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        return new ResponseEntity<>(adminGrupoService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoDTO> getGrupoById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminGrupoService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<GrupoDTO> createGrupo(@Valid @RequestBody GrupoCreateDTO grupo) {
        return new ResponseEntity<>(adminGrupoService.create(grupo), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrupoDTO> updateGrupo(@PathVariable("id") Integer id,
                                                @Valid @RequestBody GrupoCreateDTO grupo) {
        return new ResponseEntity<>(adminGrupoService.update(id, grupo), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGrupo(@PathVariable("id") Integer id) {
        adminGrupoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
