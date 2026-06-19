package com.healthybites.api;

import com.healthybites.model.entity.Grupo;
import com.healthybites.service.AdminGrupoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/grupo")
public class AdminGrupoController {

    private final AdminGrupoService adminGrupoService;

    @GetMapping
    public ResponseEntity<List<Grupo>> getAllGrupo() {
        List<Grupo> grupos = adminGrupoService.getAll();
        return new ResponseEntity<List<Grupo>>(grupos, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Grupo>> paginateGrupos(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<Grupo> grupos = adminGrupoService.paginate(pageable);
        return new ResponseEntity<Page<Grupo>>(grupos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grupo> getGrupoById(@PathVariable("id") Integer id) {
        Grupo grupo = adminGrupoService.findById(id);
        return new ResponseEntity<Grupo>(grupo, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Grupo> createGrupo(@RequestBody Grupo grupo) {
        Grupo newGrupo = adminGrupoService.create(grupo);
        return new ResponseEntity<Grupo>(newGrupo, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grupo> updateGrupo(@PathVariable("id") Integer id, @RequestBody Grupo grupo) {
        Grupo updateGrupo = adminGrupoService.update(id, grupo);
        return new ResponseEntity<Grupo>(updateGrupo, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Grupo> deleteGrupo(@PathVariable("id") Integer id) {
        adminGrupoService.delete(id);
        return new ResponseEntity<Grupo>(HttpStatus.NO_CONTENT);
    }
}
