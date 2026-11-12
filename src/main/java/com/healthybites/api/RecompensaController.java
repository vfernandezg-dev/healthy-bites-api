package com.healthybites.api;

import com.healthybites.dto.RecompensaCreateUpdateDTO;
import com.healthybites.dto.RecompensaDetailsDTO;
import com.healthybites.service.RecompensaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recompensa")
@PreAuthorize("hasAnyRole('NUTRICIONISTA','CLIENTE')")
public class RecompensaController {

    private final RecompensaService recompensaService;

    @GetMapping
    public ResponseEntity<List<RecompensaDetailsDTO>> list() {
        List<RecompensaDetailsDTO> recompensas = recompensaService.getAll();
        return new ResponseEntity<>(recompensas, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('NUTRICIONISTA') and @owner.isNutritionist(#recompensaFormDTO.nutricionistaId) and (#recompensaFormDTO.contenidoId == null or @owner.ownsContenido(#recompensaFormDTO.contenidoId)) and (#recompensaFormDTO.planAlimenticioId == null or @owner.ownsPlan(#recompensaFormDTO.planAlimenticioId))")
    public ResponseEntity<RecompensaDetailsDTO> create(@Valid @RequestBody RecompensaCreateUpdateDTO recompensaFormDTO) {
        RecompensaDetailsDTO recompensaDetailsDTO = recompensaService.create(recompensaFormDTO);
        return new ResponseEntity<>(recompensaDetailsDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RecompensaDetailsDTO> get(@PathVariable Integer id) {
        RecompensaDetailsDTO recompensa = recompensaService.findById(id);
        return new ResponseEntity<>(recompensa, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NUTRICIONISTA') and @owner.ownsRecompensa(#id) and @owner.isNutritionist(#recompensaFormDTO.nutricionistaId) and (#recompensaFormDTO.contenidoId == null or @owner.ownsContenido(#recompensaFormDTO.contenidoId)) and (#recompensaFormDTO.planAlimenticioId == null or @owner.ownsPlan(#recompensaFormDTO.planAlimenticioId))")
    public ResponseEntity<RecompensaDetailsDTO> update(@PathVariable Integer id, @Valid @RequestBody RecompensaCreateUpdateDTO recompensaFormDTO) {
        RecompensaDetailsDTO updatedRecompensa = recompensaService.update(id, recompensaFormDTO);
        return new ResponseEntity<>(updatedRecompensa, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NUTRICIONISTA') and @owner.ownsRecompensa(#id)")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        recompensaService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


}
