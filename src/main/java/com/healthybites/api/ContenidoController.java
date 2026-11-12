package com.healthybites.api;

import com.healthybites.dto.ContenidoCreateUpdateDTO;
import com.healthybites.dto.ContenidoDTO;
import com.healthybites.security.OwnershipPolicy;
import com.healthybites.service.ContenidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@RestController
@RequestMapping("/contenido")
@PreAuthorize("hasAnyRole('NUTRICIONISTA','CLIENTE')")
public class ContenidoController {

    private final ContenidoService contenidoService;
    private final OwnershipPolicy ownershipPolicy;

    @GetMapping
    public ResponseEntity<List<ContenidoDTO>> list(Authentication authentication) {
        List<ContenidoDTO> contenidos = hideLockedFiles(contenidoService.getAll(), authentication);
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

    @GetMapping("/nutricionista/{nutricionistaId}")
    public ResponseEntity<List<ContenidoDTO>> listByNutricionistaId(@PathVariable Integer nutricionistaId,
                                                                    Authentication authentication) {
        List<ContenidoDTO> contenidos = hideLockedFiles(contenidoService.getAllByNutricionistaId(nutricionistaId),
                authentication);
        return new ResponseEntity<>(contenidos, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('NUTRICIONISTA') and @owner.isNutritionist(#contenidoFormDTO.nutricionistaId)")
    public ResponseEntity<ContenidoDTO> create(@Valid @RequestBody ContenidoCreateUpdateDTO contenidoFormDTO) {
        ContenidoDTO contenidoDetailsDTO = contenidoService.create(contenidoFormDTO);
        return new ResponseEntity<>(contenidoDetailsDTO, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('NUTRICIONISTA') or (hasRole('CLIENTE') and @owner.canReadContenido(#id))")
    public ResponseEntity<ContenidoDTO> get(@PathVariable Integer id) {
        ContenidoDTO contenido = contenidoService.findById(id);
        return new ResponseEntity<>(contenido, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('NUTRICIONISTA') and @owner.ownsContenido(#id) and @owner.isNutritionist(#contenidoFormDTO.nutricionistaId)")
    public ResponseEntity<ContenidoDTO> update(@PathVariable Integer id, @Valid @RequestBody ContenidoCreateUpdateDTO contenidoFormDTO) {
        ContenidoDTO updatedContenido = contenidoService.update(id, contenidoFormDTO);
        return new ResponseEntity<>(updatedContenido, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('NUTRICIONISTA') and @owner.ownsContenido(#id)")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        contenidoService.delete(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * El catálogo se puede listar entero, pero a un cliente no se le entrega el fichero de un contenido de pago que
     * no puede abrir: antes bastaba con listar para saltarse la suscripción. El detalle sigue en {@code GET /{id}}.
     */
    private List<ContenidoDTO> hideLockedFiles(List<ContenidoDTO> contenidos, Authentication authentication) {
        boolean client = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CLIENTE".equals(authority.getAuthority()));
        if (client) {
            Set<Integer> locked = ownershipPolicy.lockedContenidoIds();
            contenidos.stream().filter(contenido -> locked.contains(contenido.getId()))
                    .forEach(contenido -> contenido.setFilePath(null));
        }
        return contenidos;
    }
}
