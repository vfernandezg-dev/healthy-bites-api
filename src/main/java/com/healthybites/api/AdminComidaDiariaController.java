package com.healthybites.api;

import com.healthybites.dto.ComidaDiariaCreateDTO;
import com.healthybites.dto.ComidaDiariaDTO;
import com.healthybites.service.AdminComidaDiariaService;
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
@RequestMapping("/admin/comida-diaria")
@PreAuthorize("hasRole('ADMIN')")
public class AdminComidaDiariaController {

    private final AdminComidaDiariaService adminComidaDiariaService;

    @GetMapping
    public ResponseEntity<List<ComidaDiariaDTO>> getAllComidaDiaria() {
        return new ResponseEntity<>(adminComidaDiariaService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ComidaDiariaDTO>> paginateComidasDiarias(
            @PageableDefault(size = 5, sort = "nombreComida") Pageable pageable) {
        return new ResponseEntity<>(adminComidaDiariaService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComidaDiariaDTO> getComidaDiariaById(@PathVariable("id") Integer id) {
        return new ResponseEntity<>(adminComidaDiariaService.findById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ComidaDiariaDTO> createComidaDiaria(@RequestParam Integer planAlimenticioId,
                                                              @Valid @RequestBody ComidaDiariaCreateDTO comidaDiaria) {
        return new ResponseEntity<>(adminComidaDiariaService.create(planAlimenticioId, comidaDiaria), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComidaDiariaDTO> updateComidaDiaria(@PathVariable("id") Integer id,
                                                              @RequestParam(required = false) Integer planAlimenticioId,
                                                              @Valid @RequestBody ComidaDiariaCreateDTO comidaDiaria) {
        return new ResponseEntity<>(adminComidaDiariaService.update(id, planAlimenticioId, comidaDiaria), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComidaDiaria(@PathVariable("id") Integer id) {
        adminComidaDiariaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
