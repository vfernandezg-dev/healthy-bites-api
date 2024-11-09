package com.healthybites.api;

import com.healthybites.dto.HabitoCreateDTO;
import com.healthybites.dto.HabitoDTO;
import com.healthybites.service.HabitoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/habito")
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
public class HabitoController {

    private final HabitoService habitoService;

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<HabitoDTO>> getAllHabitosByClienteId(@PathVariable Integer clienteId) {
        List<HabitoDTO> habitos = habitoService.getAll(clienteId);
        return ResponseEntity.ok(habitos);
    }

    @PostMapping
    public ResponseEntity<HabitoDTO> registrarHabito(@Valid @RequestBody HabitoCreateDTO habitoCreateDTO) {
        HabitoDTO habitoDTO = habitoService.registrarHabito(habitoCreateDTO);
        return new ResponseEntity<>(habitoDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{habitoId}")
    public ResponseEntity<HabitoDTO> actualizarHabito(@PathVariable Integer habitoId, @Valid @RequestBody HabitoCreateDTO habitoCreateDTO) {
        HabitoDTO habitoDTO = habitoService.actualizarHabito(habitoId, habitoCreateDTO);
        return new ResponseEntity<>(habitoDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{habitoId}")
    public ResponseEntity<Void> deleteHabito(@PathVariable Integer habitoId) {
        habitoService.delete(habitoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
