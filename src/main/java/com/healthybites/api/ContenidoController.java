package com.healthybites.api;

import com.healthybites.model.entity.Contenido;
import com.healthybites.service.ContenidoService;
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
@RequestMapping("/contenido")
public class ContenidoController {

    private final ContenidoService contenidoService;

    @PostMapping
    public ResponseEntity<Contenido> createContenido(@RequestBody Contenido contenido) {
        Contenido savedContenido = contenidoService.createContenido(contenido);
        return new ResponseEntity<>(savedContenido, HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Contenido>> getAllContenidoByUser(@PathVariable("userId") Integer userId) {
        List<Contenido> contenidos = contenidoService.getAllContenidoByUser(userId);
        return ResponseEntity.ok(contenidos);
    }

    @GetMapping("/{collectionId}")
    public ResponseEntity<Contenido> getContenidoById(@PathVariable("collectionId") Integer collectionId) {
        Contenido contenido = contenidoService.getContenidoById(collectionId);
        return ResponseEntity.ok(contenido);
    }

    @PutMapping("/{collectionId}")
    public ResponseEntity<Contenido> updateContenido(@PathVariable("collectionId") Integer collectionId, @RequestBody Contenido contenido) {
        Contenido updatedContenido = contenidoService.updateContenido(collectionId, contenido);
        return ResponseEntity.ok(updatedContenido);
    }

    @DeleteMapping("/{collectionId}")
    public ResponseEntity<Void> deleteContenido(@PathVariable("collectionId") Integer collectionId) {
        contenidoService.deleteContenido(collectionId);
        return ResponseEntity.noContent().build();
    }

}
