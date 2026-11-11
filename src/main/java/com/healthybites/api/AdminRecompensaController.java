package com.healthybites.api;

import com.healthybites.dto.RecompensaCreateUpdateDTO;
import com.healthybites.dto.RecompensaDetailsDTO;
import com.healthybites.service.AdminRecompensaService;
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
@RequestMapping("/admin/recompensa")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecompensaController {

    private final AdminRecompensaService adminRecompensaService;

    @PostMapping
    public ResponseEntity<RecompensaDetailsDTO> createRecompensa(@Valid @RequestBody RecompensaCreateUpdateDTO recompensaDTO) {
        return new ResponseEntity<>(adminRecompensaService.create(recompensaDTO), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RecompensaDetailsDTO>> listarRecompensas() {
        return ResponseEntity.ok(adminRecompensaService.getAll());
    }

    @GetMapping("/page")
    public ResponseEntity<Page<RecompensaDetailsDTO>> paginateRecompensas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        return new ResponseEntity<>(adminRecompensaService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecompensaDetailsDTO> getRecompensa(@PathVariable Integer id) {
        return ResponseEntity.ok(adminRecompensaService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecompensaDetailsDTO> updateRecompensa(@PathVariable Integer id,
                                                                 @Valid @RequestBody RecompensaCreateUpdateDTO recompensaDTO) {
        return ResponseEntity.ok(adminRecompensaService.update(id, recompensaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarRecompensa(@PathVariable Integer id) {
        adminRecompensaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{recompensaId}/asignar/{clienteId}")
    public ResponseEntity<RecompensaDetailsDTO> asignarRecompensaACliente(@PathVariable Integer recompensaId,
                                                                          @PathVariable Integer clienteId) {
        return ResponseEntity.ok(adminRecompensaService.asignarRecompensaACliente(recompensaId, clienteId));
    }
}
