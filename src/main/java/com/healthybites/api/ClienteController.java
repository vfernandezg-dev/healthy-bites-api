package com.healthybites.api;

import com.healthybites.dto.ClienteMetaDTO;
import com.healthybites.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('CLIENTE','ADMIN')")
public class ClienteController {

    private final ClienteService clienteService;

    // Actualiza el perfil del cliente y, si se indica metaID, tambien esa meta
    @PutMapping("/{id}/perfil")
    public ResponseEntity<ClienteMetaDTO> updateProfileAndMeta(@PathVariable Integer id,
                                                               @Valid @RequestBody ClienteMetaDTO clienteMetaDTO) {
        ClienteMetaDTO actualizado = clienteService.actualizarClienteConMeta(id, clienteMetaDTO);
        return ResponseEntity.ok(actualizado);
    }
}
