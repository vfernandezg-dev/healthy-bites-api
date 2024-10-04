package com.healthybites.api;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.dto.MetaDTO;
import com.healthybites.service.ClienteService;
import com.healthybites.service.MetaService;
import com.healthybites.service.impl.AdminClienteServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
public class ClienteController {
    private final AdminClienteServiceImpl adminClienteService;

    @PutMapping("/{id}/perfil")
    public ResponseEntity<ClienteDTO> updateProfileAndMeta(@PathVariable Integer id, @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO updatedCliente = adminClienteService.updateProfileAndMeta(id, clienteDTO);
        return ResponseEntity.ok(updatedCliente);
    }
}
