package com.healthybites.api;

import com.healthybites.model.entity.Grupo;
import com.healthybites.model.entity.Unirse;
import com.healthybites.service.UnirseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/unirse")
public class UnirseController {

    private final UnirseService unirseService;

    @PostMapping("/{clienteId}/add-grupo")
    public ResponseEntity<Unirse> addClienteToGrupo(@PathVariable Integer clienteId, @RequestParam Integer grupoId) {
        Unirse unirse = unirseService.addClienteToGrupo(clienteId, grupoId);
        return new ResponseEntity<>(unirse, HttpStatus.CREATED);
    }

    @DeleteMapping("/{clienteId}/remove-grupo/{grupoId}")
    public ResponseEntity<Void> removeClienteFromGrupo(@PathVariable Integer clienteId, @PathVariable Integer grupoId) {
        unirseService.removeClienteFromGrupo(clienteId, grupoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{clienteId}/grupo")
    public ResponseEntity<List<Grupo>> getGrupoByClienteId(@PathVariable Integer clienteId) {
        List<Grupo> grupos = unirseService.getGrupoFromCliente(clienteId);
        return ResponseEntity.ok(grupos);
    }

}
