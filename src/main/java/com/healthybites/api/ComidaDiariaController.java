package com.healthybites.api;

import com.healthybites.dto.ComidaDiariaCreateUpdateDTO;
import com.healthybites.dto.ComidaDiariaDetailsDTO;
import com.healthybites.service.ComidaDiariaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/comida-diaria")
public class ComidaDiariaController {

    private final ComidaDiariaService comidaDiariaService;

    @GetMapping
    public ResponseEntity<List<ComidaDiariaDetailsDTO>> list() {
        List<ComidaDiariaDetailsDTO> comidasDiarias = comidaDiariaService.getAll();
        return new ResponseEntity<>(comidasDiarias, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ComidaDiariaDetailsDTO> create(@RequestBody ComidaDiariaCreateUpdateDTO comidaFromDTO) {
        ComidaDiariaDetailsDTO comidaDetailsDTO = comidaDiariaService.create(comidaFromDTO);
        return new ResponseEntity<>(comidaDetailsDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComidaDiariaDetailsDTO> get(@PathVariable Integer id) {
        ComidaDiariaDetailsDTO comida = comidaDiariaService.findById(id);
        return new ResponseEntity<>(comida, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ComidaDiariaDetailsDTO> update(@PathVariable Integer id, @RequestBody ComidaDiariaCreateUpdateDTO comidaFromDTO) {
        ComidaDiariaDetailsDTO updatedComida = comidaDiariaService.update(id, comidaFromDTO);
        return new ResponseEntity<>(updatedComida, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        comidaDiariaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
