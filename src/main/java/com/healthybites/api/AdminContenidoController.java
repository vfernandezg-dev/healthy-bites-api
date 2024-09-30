package com.healthybites.api;

import com.healthybites.model.entity.Contenido;
import com.healthybites.service.AdminContenidoService;
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
@RequestMapping("/admin/contenido")
public class AdminContenidoController {

    private final AdminContenidoService adminContenidoService;

    @GetMapping
    public ResponseEntity<List<Contenido>> getAllContenido() {
        List<Contenido> contenidos = adminContenidoService.getAll();
        return new ResponseEntity<List<Contenido>>(contenidos, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Contenido>> paginateContenidos(
            @PageableDefault(size = 5, sort = "titulo") Pageable pageable) {
        Page<Contenido> contenidos = adminContenidoService.paginate(pageable);
        return new ResponseEntity<Page<Contenido>>(contenidos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Contenido> getContenidoById(@PathVariable("id") Integer id) {
        Contenido contenido = adminContenidoService.findById(id);
        return new ResponseEntity<Contenido>(contenido, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Contenido> createContenido(@RequestBody Contenido contenido) {
        Contenido newContenido = adminContenidoService.create(contenido);
        return new ResponseEntity<Contenido>(newContenido, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Contenido> updateContenido(@PathVariable("id") Integer id, @RequestBody Contenido contenido) {
        Contenido updateContenido = adminContenidoService.update(id, contenido);
        return new ResponseEntity<Contenido>(updateContenido, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Contenido> deleteContenido(@PathVariable("id") Integer id) {
        adminContenidoService.delete(id);
        return new ResponseEntity<Contenido>(HttpStatus.NO_CONTENT);
    }
}
