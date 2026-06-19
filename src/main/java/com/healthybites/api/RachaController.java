package com.healthybites.api;

import com.healthybites.dto.RecompensaDetailsDTO;
import com.healthybites.service.RachaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/racha")
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
public class RachaController {

    private final RachaService rachaService;

    // Metodo para obtener la recompensa a un cliente
    //http://localhost:8080/racha/1/recompensa/1
    @PostMapping("/{clientId}/recompensa/{recompensaId}")
    public ResponseEntity<RecompensaDetailsDTO> addRecompensaToClient(@PathVariable Integer clientId,
                                                                      @PathVariable Integer recompensaId) {
        RecompensaDetailsDTO recompensaDetailsDTO = rachaService.addRecompensaToClient(clientId, recompensaId);
        return new ResponseEntity<>(recompensaDetailsDTO, HttpStatus.CREATED);
    }

    // Metodo para eliminar la recompensa de un cliente
    //http://localhost:8080/racha/1/remove/1
    @DeleteMapping("/{clientId}/remove/{recompensaId}")
    public ResponseEntity<Void> removeRecompensaFromClient(@PathVariable Integer clientId,
                                                           @PathVariable Integer recompensaId) {
        rachaService.removeRecompensaFromClient(clientId, recompensaId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    // Metodo para obtener todas las recompensas de un cliente
    //http://localhost:8080/racha/1/all-recompensas
    @GetMapping("/{clientId}/all-recompensas")
    public ResponseEntity<List<RecompensaDetailsDTO>> getAllRecompensasByClient(@PathVariable Integer clientId) {
        List<RecompensaDetailsDTO> recompensas = rachaService.getAllRecompensasByClient(clientId);
        return new ResponseEntity<>(recompensas, HttpStatus.OK);
    }
}
