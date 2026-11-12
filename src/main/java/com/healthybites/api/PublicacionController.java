package com.healthybites.api;

import com.healthybites.dto.PublicacionCreateDTO;
import com.healthybites.dto.PublicacionDTO;
import com.healthybites.security.CurrentUser;
import com.healthybites.service.PublicacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/publicacion")
@PreAuthorize("hasRole('CLIENTE')")
public class PublicacionController {

    private final PublicacionService publicacionService;

    @GetMapping
    public ResponseEntity<List<PublicacionDTO>> getAllPublicacion() {
        List<PublicacionDTO> publicaciones = publicacionService.getFeed(CurrentUser.id());
        return new ResponseEntity<>(publicaciones, HttpStatus.OK);
    }

    @GetMapping("/cliente/{id}")
    @PreAuthorize("hasRole('CLIENTE') and @owner.isClient(#id)")
    public ResponseEntity<List<PublicacionDTO>> getAll(@PathVariable Integer id) {
        List<PublicacionDTO> publicaciones = publicacionService.getAll(id);
        return new ResponseEntity<>(publicaciones, HttpStatus.OK);
    }

    @GetMapping("/grupo/{grupoId}")
    @PreAuthorize("hasRole('CLIENTE') and @owner.canReadGrupo(#grupoId)")
    public ResponseEntity<List<PublicacionDTO>> getPublicacionesByGrupoId(@PathVariable Integer grupoId) {
        List<PublicacionDTO> publicaciones = publicacionService.getPublicacionesByGrupoId(grupoId);
        return new ResponseEntity<>(publicaciones, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENTE') and @owner.isClient(#publicacionCreateDTO.clienteId) and @owner.canReadGrupo(#publicacionCreateDTO.grupoId)")
    public ResponseEntity<PublicacionDTO> create(@Valid @RequestBody PublicacionCreateDTO publicacionCreateDTO) {
        PublicacionDTO publicacionDTO = publicacionService.create(publicacionCreateDTO);
        return new ResponseEntity<>(publicacionDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{publicacionId}/cliente/{clienteId}")
    @PreAuthorize("hasRole('CLIENTE') and @owner.isClient(#clienteId) and @owner.ownsPublicacion(#publicacionId)")
    public ResponseEntity<PublicacionDTO> getById(@PathVariable Integer clienteId, @PathVariable Integer publicacionId) {
        PublicacionDTO publicacion = publicacionService.findByIdAndClienteId(publicacionId, clienteId);
        return new ResponseEntity<>(publicacion, HttpStatus.OK);
    }

    @PutMapping("/{publicacionId}/cliente/{clienteId}")
    @PreAuthorize("hasRole('CLIENTE') and @owner.isClient(#clienteId) and @owner.ownsPublicacion(#publicacionId) and @owner.isClient(#updatedPublicacionDTO.clienteId)")
    public ResponseEntity<PublicacionDTO> update(@PathVariable Integer clienteId, @PathVariable Integer publicacionId, @Valid @RequestBody PublicacionCreateDTO updatedPublicacionDTO) {
        PublicacionDTO publicacion = publicacionService.update(publicacionId, clienteId, updatedPublicacionDTO);
        return new ResponseEntity<>(publicacion, HttpStatus.OK);
    }

    @DeleteMapping("/{publicacionId}/cliente/{clienteId}")
    @PreAuthorize("hasRole('CLIENTE') and @owner.isClient(#clienteId) and @owner.ownsPublicacion(#publicacionId)")
    public ResponseEntity<Void> delete(@PathVariable Integer clienteId, @PathVariable Integer publicacionId) {
        publicacionService.delete(publicacionId, clienteId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
