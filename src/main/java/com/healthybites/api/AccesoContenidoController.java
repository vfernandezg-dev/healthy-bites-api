package com.healthybites.api;

import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.Contenido;
import com.healthybites.service.AccesoContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/acceso-contenido")
public class AccesoContenidoController {

    private final AccesoContenidoService accesoContenidoService;

    @PostMapping("/{clienteId}/add-contenido")
    public ResponseEntity<AccesoContenido> addContenidoToCliente(@PathVariable Integer clienteId, @RequestParam Integer contenidoId) {
        AccesoContenido accesoContenido = accesoContenidoService.addContenidoToCliente(clienteId, contenidoId);
        return new ResponseEntity<>(accesoContenido, HttpStatus.CREATED);
    }

    @DeleteMapping("/{clienteId}/remove-contenido/{contenidoId}")
    public ResponseEntity<Void> removeContenidoFromCliente(@PathVariable Integer clienteId, @PathVariable Integer contenidoId) {
        accesoContenidoService.removeContenidoFromCliente(clienteId, contenidoId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{clienteId}/contenido")
    public ResponseEntity<List<Contenido>> getContenidoInCliente(@PathVariable Integer clienteId) {
        List<Contenido> contenidos = accesoContenidoService.getContenidoInCliente(clienteId);
        return ResponseEntity.ok(contenidos);
    }
}
