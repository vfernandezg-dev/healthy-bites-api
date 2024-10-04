package com.healthybites.api;

import com.healthybites.model.entity.Comentario;
import com.healthybites.service.AdminComentarioService;
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
@RequestMapping("/admin/comentario")
public class AdminComentarioController {

    private final AdminComentarioService adminComentarioService;

    @GetMapping
    public ResponseEntity<List<Comentario>> getAllComentario() {
        List<Comentario> comentarios = adminComentarioService.getAll();
        return new ResponseEntity<List<Comentario>>(comentarios, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<Comentario>> paginateComentarios(
            @PageableDefault(size = 5, sort = "mensaje") Pageable pageable) {
        Page<Comentario> comentarios = adminComentarioService.paginate(pageable);
        return new ResponseEntity<Page<Comentario>>(comentarios, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comentario> getComentarioById(@PathVariable("id") Integer id) {
        Comentario comentarios = adminComentarioService.findById(id);
        return new ResponseEntity<Comentario>(comentarios, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Comentario> createComentario(@RequestBody Comentario comentarios) {
        Comentario newComentario = adminComentarioService.create(comentarios);
        return new ResponseEntity<Comentario>(newComentario, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comentario> updateComentario(@PathVariable("id") Integer id, @RequestBody Comentario comentarios) {
        Comentario updateComentario = adminComentarioService.update(id, comentarios);
        return new ResponseEntity<Comentario>(updateComentario, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Comentario> deleteComentario(@PathVariable("id") Integer id) {
        adminComentarioService.delete(id);
        return new ResponseEntity<Comentario>(HttpStatus.NO_CONTENT);
    }
}
