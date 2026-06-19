package com.healthybites.api;

import com.healthybites.model.entity.Publicacion;
import com.healthybites.service.AdminPublicacionService;
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
@RequestMapping("/admin/publicacion")
public class AdminPublicacionController {

    private final AdminPublicacionService adminPublicacionService;

    @GetMapping
    public ResponseEntity<List<Publicacion>> getAllPublicacion() {
        List<Publicacion> publicaciones = adminPublicacionService.getAll();
        return new ResponseEntity<List<Publicacion>>(publicaciones, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Publicacion>> paginatePublicaciones(
            @PageableDefault(size = 5, sort = "titulo") Pageable pageable) {
        Page<Publicacion> publicaciones = adminPublicacionService.paginate(pageable);
        return new ResponseEntity<Page<Publicacion>>(publicaciones, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Publicacion> getPublicacionById(@PathVariable("id") Integer id) {
        Publicacion publicacion = adminPublicacionService.findById(id);
        return new ResponseEntity<Publicacion>(publicacion, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Publicacion> createPublicacion(@RequestBody Publicacion publicacion) {
        Publicacion newPublicacion = adminPublicacionService.create(publicacion);
        return new ResponseEntity<Publicacion>(newPublicacion, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Publicacion> updatePublicacion(@PathVariable("id") Integer id, @RequestBody Publicacion publicacion) {
        Publicacion updatePublicacion = adminPublicacionService.update(id, publicacion);
        return new ResponseEntity<Publicacion>(updatePublicacion, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Publicacion> deletePublicacion(@PathVariable("id") Integer id) {
        adminPublicacionService.delete(id);
        return new ResponseEntity<Publicacion>(HttpStatus.NO_CONTENT);
    }
}
