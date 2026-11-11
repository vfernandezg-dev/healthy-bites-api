package com.healthybites.api;

import com.healthybites.dto.PublicacionCreateDTO;
import com.healthybites.dto.PublicacionDTO;
import com.healthybites.service.AdminPublicacionService;
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
@RequestMapping("/admin/publicacion")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPublicacionController {

    private final AdminPublicacionService adminPublicacionService;

    @GetMapping
    public ResponseEntity<List<PublicacionDTO>> getAllPublicacion() {
        return new ResponseEntity<>(adminPublicacionService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<PublicacionDTO>> paginatePublicaciones(
            @PageableDefault(size = 5, sort = "titulo") Pageable pageable) {
        return new ResponseEntity<>(adminPublicacionService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PublicacionDTO> getPublicacionById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminPublicacionService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PublicacionDTO> createPublicacion(@Valid @RequestBody PublicacionCreateDTO publicacion) {
        return new ResponseEntity<>(adminPublicacionService.create(publicacion), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PublicacionDTO> updatePublicacion(@PathVariable("id") Integer id,
                                                            @Valid @RequestBody PublicacionCreateDTO publicacion) {
        return new ResponseEntity<>(adminPublicacionService.update(id, publicacion), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePublicacion(@PathVariable("id") Integer id) {
        adminPublicacionService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
