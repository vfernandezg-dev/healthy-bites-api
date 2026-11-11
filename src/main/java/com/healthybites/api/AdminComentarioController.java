package com.healthybites.api;

import com.healthybites.dto.ComentarioCreateDTO;
import com.healthybites.dto.ComentarioDTO;
import com.healthybites.service.AdminComentarioService;
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
@RequestMapping("/admin/comentario")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComentarioController {

    private final AdminComentarioService adminComentarioService;

    @GetMapping
    public ResponseEntity<List<ComentarioDTO>> getAllComentario() {
        return new ResponseEntity<>(adminComentarioService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ComentarioDTO>> paginateComentarios(
            @PageableDefault(size = 5, sort = "mensaje") Pageable pageable) {
        return new ResponseEntity<>(adminComentarioService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComentarioDTO> getComentarioById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminComentarioService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ComentarioDTO> createComentario(@RequestParam Integer publicacionId,
                                                          @Valid @RequestBody ComentarioCreateDTO comentario) {
        return new ResponseEntity<>(adminComentarioService.create(publicacionId, comentario), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComentarioDTO> updateComentario(@PathVariable("id") Integer id,
                                                          @RequestParam(required = false) Integer publicacionId,
                                                          @Valid @RequestBody ComentarioCreateDTO comentario) {
        return new ResponseEntity<>(adminComentarioService.update(id, publicacionId, comentario), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComentario(@PathVariable("id") Integer id) {
        adminComentarioService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
