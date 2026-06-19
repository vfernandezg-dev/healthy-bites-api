package com.healthybites.api;

import com.healthybites.dto.ContenidoDTO;
import com.healthybites.model.enums.CategoriaContenido;
import com.healthybites.model.enums.TipoContenido;
import com.healthybites.service.AdminContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/contenido")
public class AdminContenidoController {
    @Autowired
    private final AdminContenidoService adminContenidoService;

    @GetMapping
    public ResponseEntity<List<ContenidoDTO>> getAllContenido() {
        List<ContenidoDTO> contenidos = adminContenidoService.getAll();
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ContenidoDTO>> paginateContenidos(
            @PageableDefault(size = 5, sort = "titulo") Pageable pageable) {
        Page<ContenidoDTO> contenidos = adminContenidoService.paginate(pageable);
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContenidoDTO> getContenidoById(@PathVariable("id") Integer id) {
        ContenidoDTO contenido = adminContenidoService.findById(id);
        return new ResponseEntity<>(contenido, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ContenidoDTO> createContenido(@Validated @RequestBody ContenidoDTO contenidoDTO) {
        ContenidoDTO newContenido = adminContenidoService.create(contenidoDTO);
        return ResponseEntity.ok(newContenido);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContenidoDTO> updateContenido(@PathVariable("id") Integer id, @RequestBody ContenidoDTO contenidoDTO) {
        ContenidoDTO updateContenido = adminContenidoService.update(id, contenidoDTO);
        return new ResponseEntity<>(updateContenido, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContenido(@PathVariable("id") Integer id) {
        adminContenidoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ContenidoDTO>> obtenerContenido(
            @RequestParam(required = false) TipoContenido tipoContenido,
            @RequestParam(required = false) CategoriaContenido categoriaContenido) {
        List<ContenidoDTO> contenidos = adminContenidoService.obtenerContenido(tipoContenido, categoriaContenido);
        return ResponseEntity.ok(contenidos);
    }

}
