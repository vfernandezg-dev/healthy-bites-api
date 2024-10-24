package com.healthybites.api;

import com.healthybites.dto.ContenidoDetailsDTO;
import com.healthybites.model.entity.AccesoContenido;
import com.healthybites.model.entity.Contenido;
import com.healthybites.service.AccesoContenidoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/acceso-contenido")
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
public class AccesoContenidoController {

    private final AccesoContenidoService accesoContenidoService;

    // Metodo para agregar un contenido a un cliente
    //http://localhost:8080/acceso-contenido/1/add-contenido?contentId=1
    @PostMapping("/{clientId}/add-contenido")
    public ResponseEntity<ContenidoDetailsDTO> addContentToClient(@PathVariable Integer clientId,
                                                                  @RequestParam Integer contentId) {
        ContenidoDetailsDTO contenidoDetailsDTO = accesoContenidoService.addContentToClient(clientId, contentId);
        return new ResponseEntity<>(contenidoDetailsDTO, HttpStatus.CREATED);
    }

    // Metodo para eliminar un contenido de un cliente
    //http://localhost:8080/acceso-contenido/1/remove-contenido/1
    @DeleteMapping("/{clientId}/remove-contenido/{contentId}")
    public ResponseEntity<Void> removeContentFromClient(@PathVariable Integer clientId,
                                                        @PathVariable Integer contentId) {
        accesoContenidoService.removeContentFromClient(clientId, contentId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Metodo para obtener todos los contenidos de un cliente
    //http://localhost:8080/acceso-contenido/1/all-contenidos
    @GetMapping("/{clientId}/all-contenidos")
    public ResponseEntity<List<ContenidoDetailsDTO>> getAllContentByClient(@PathVariable Integer clientId) {
        List<ContenidoDetailsDTO> contenidos = accesoContenidoService.getAllContentByClient(clientId);
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

}
