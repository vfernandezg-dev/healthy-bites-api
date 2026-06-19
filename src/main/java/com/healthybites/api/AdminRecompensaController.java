package com.healthybites.api;

import com.healthybites.dto.RecompensaDTO;
import com.healthybites.model.entity.Recompensa;
import com.healthybites.service.AdminRecompensaService;
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
@RequestMapping("/admin/recompensa")
public class AdminRecompensaController {

    @Autowired
    private final AdminRecompensaService adminRecompensaService;

    @PostMapping
    public ResponseEntity<RecompensaDTO> createRecompensa(@Validated @RequestBody RecompensaDTO recompensaDTO) {
        RecompensaDTO nuevaRecompensa = adminRecompensaService.create(recompensaDTO);
        return ResponseEntity.ok(nuevaRecompensa);
    }

    @GetMapping
    public ResponseEntity<List<RecompensaDTO>> listarRecompensas() {
        List<RecompensaDTO> recompensas = adminRecompensaService.getAll();
        return ResponseEntity.ok(recompensas);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<RecompensaDTO>> paginateRachas(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<RecompensaDTO> recompensas = adminRecompensaService.paginate(pageable);
        return new ResponseEntity<>(recompensas, HttpStatus.OK);
    }


    @GetMapping("/{id}")
    public ResponseEntity<RecompensaDTO> getRecompensa(@PathVariable Integer id) {
        RecompensaDTO recompensa = adminRecompensaService.findById(id);
        return ResponseEntity.ok(recompensa);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RecompensaDTO> updateRecompensa(
            @PathVariable Integer id,
            @Validated @RequestBody RecompensaDTO recompensaDTO) {
        RecompensaDTO recompensaActualizada = adminRecompensaService.update(id, recompensaDTO);
        return ResponseEntity.ok(recompensaActualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Recompensa> eliminarRecompensa(@PathVariable Integer id) {
        adminRecompensaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{recompensaId}/asignar/{clienteId}")
    public ResponseEntity<RecompensaDTO> asignarRecompensaACliente(
            @PathVariable Integer recompensaId,
            @PathVariable Integer clienteId) {
        RecompensaDTO recompensaAsignada = adminRecompensaService.asignarRecompensaACliente(recompensaId, clienteId);
        return ResponseEntity.ok(recompensaAsignada);
    }

}
