package com.healthybites.api;

import com.healthybites.dto.HabitoDTO;
import com.healthybites.model.entity.Habito;
import com.healthybites.service.AdminHabitoService;
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
@RequestMapping("/admin/habito")
public class AdminHabitoController {

    @Autowired
    private final AdminHabitoService adminHabitoService;

    @PostMapping
    public ResponseEntity<HabitoDTO> createHabito(@Validated @RequestBody HabitoDTO habitoDTO) {
        HabitoDTO nuevoHabito = adminHabitoService.create(habitoDTO);
        return ResponseEntity.ok(nuevoHabito);
    }

    @GetMapping
    public ResponseEntity<List<HabitoDTO>> listarHabitos() {
        List<HabitoDTO> habitos = adminHabitoService.getAll();
        return ResponseEntity.ok(habitos);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<HabitoDTO>> paginateHabitos(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<HabitoDTO> habitos = adminHabitoService.paginate(pageable);
        return new ResponseEntity<>(habitos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitoDTO> getHabito(@PathVariable Integer id) {
        HabitoDTO habito = adminHabitoService.findById(id);
        return ResponseEntity.ok(habito);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitoDTO> updateHabito(
            @PathVariable Integer id,
            @Validated @RequestBody HabitoDTO habitoDTO) {
        HabitoDTO habitoActualizado = adminHabitoService.update(id, habitoDTO);
        return ResponseEntity.ok(habitoActualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Habito> eliminarHabito(@PathVariable Integer id) {
        adminHabitoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{habitoId}/asignar/{clienteId}")
    public ResponseEntity<HabitoDTO> asignarHabitoACliente(
            @PathVariable Integer habitoId,
            @PathVariable Integer clienteId) {
        adminHabitoService.asignarHabitoACliente(habitoId, clienteId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}

