package com.healthybites.api;

import com.healthybites.dto.RachaDTO;
import com.healthybites.service.AdminRachaService;
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
@RequestMapping("/admin/racha")
@PreAuthorize("hasRole('ADMIN')")
public class AdminRachaController {

    private final AdminRachaService adminRachaService;

    @GetMapping
    public ResponseEntity<List<RachaDTO>> list() {
        return new ResponseEntity<>(adminRachaService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<RachaDTO>> paginateRachas(
            @PageableDefault(size = 5, sort = "contadorDias") Pageable pageable) {
        return new ResponseEntity<>(adminRachaService.paginate(pageable), HttpStatus.OK);
    }

    @GetMapping("/cliente/{clienteId}/recompensa/{recompensaId}")
    public ResponseEntity<RachaDTO> getRacha(@PathVariable Integer clienteId, @PathVariable Integer recompensaId) {
        return new ResponseEntity<>(adminRachaService.findById(clienteId, recompensaId), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RachaDTO> create(@Valid @RequestBody RachaDTO rachaDTO) {
        return new ResponseEntity<>(adminRachaService.create(rachaDTO), HttpStatus.CREATED);
    }

    @PutMapping("/cliente/{clienteId}/recompensa/{recompensaId}")
    public ResponseEntity<RachaDTO> updateRacha(@PathVariable Integer clienteId,
                                                @PathVariable Integer recompensaId,
                                                @Valid @RequestBody RachaDTO rachaDTO) {
        return new ResponseEntity<>(adminRachaService.update(clienteId, recompensaId, rachaDTO), HttpStatus.OK);
    }

    @DeleteMapping("/cliente/{clienteId}/recompensa/{recompensaId}")
    public ResponseEntity<Void> deleteRacha(@PathVariable Integer clienteId, @PathVariable Integer recompensaId) {
        adminRachaService.delete(clienteId, recompensaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{recompensaId}/asignar/{clienteId}")
    public ResponseEntity<RachaDTO> asignarRacha(@PathVariable Integer recompensaId,
                                                 @PathVariable Integer clienteId) {
        return new ResponseEntity<>(adminRachaService.asignarRachaACliente(recompensaId, clienteId), HttpStatus.CREATED);
    }
}
