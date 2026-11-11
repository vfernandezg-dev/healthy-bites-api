package com.healthybites.api;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import com.healthybites.service.AdminHabitoService;
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
@RequestMapping("/admin/habito")
@PreAuthorize("hasRole('ADMIN')")
public class AdminHabitoController {

    private final AdminHabitoService adminHabitoService;

    @PostMapping
    public ResponseEntity<HabitoDTO> createHabito(@Valid @RequestBody HabitoCreateDTO habitoCreateDTO) {
        return new ResponseEntity<>(adminHabitoService.create(habitoCreateDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<HabitoDTO>> listarHabitos() {
        return ResponseEntity.ok(adminHabitoService.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<HabitoDTO>> paginateHabitos(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        return new ResponseEntity<>(adminHabitoService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HabitoDTO> getHabito(@PathVariable Integer id) {
        return ResponseEntity.ok(adminHabitoService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HabitoDTO> updateHabito(@PathVariable Integer id,
                                                  @Valid @RequestBody HabitoCreateDTO habitoCreateDTO) {
        return ResponseEntity.ok(adminHabitoService.update(id, habitoCreateDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarHabito(@PathVariable Integer id) {
        adminHabitoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{habitoId}/asignar/{clienteId}")
    public ResponseEntity<HabitoDTO> asignarHabitoACliente(@PathVariable Integer habitoId,
                                                           @PathVariable Integer clienteId) {
        return ResponseEntity.ok(adminHabitoService.asignarHabitoACliente(habitoId, clienteId));
    }
}
