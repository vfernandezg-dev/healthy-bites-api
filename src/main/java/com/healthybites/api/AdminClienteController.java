package com.healthybites.api;

import com.healthybites.dto.ClienteDTO;
import com.healthybites.model.entity.Cliente;
import com.healthybites.service.AdminClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/cliente")
public class AdminClienteController {

    private final AdminClienteService adminClienteService;

    @GetMapping
    public ResponseEntity<List<ClienteDTO>> getAllCliente() {
        List<ClienteDTO> clientes = adminClienteService.getAll();
        return new ResponseEntity<>(clientes, HttpStatus.OK);
    }

    @GetMapping("/page")
    public ResponseEntity<Page<ClienteDTO>> paginateClientes(
            @PageableDefault(size = 5, sort = "nombre") Pageable pageable) {
        Page<ClienteDTO> clientes = adminClienteService.paginate(pageable);
        return new ResponseEntity<Page<ClienteDTO>>(clientes, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> getClienteById(@PathVariable("id") Integer id) {
        ClienteDTO cliente = adminClienteService.findById(id);
        return new ResponseEntity<>(cliente, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<ClienteDTO> createCliente(@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO createdCliente = adminClienteService.create(clienteDTO);
        return new ResponseEntity<>(createdCliente, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> updateCliente(@PathVariable("id") Integer id,@Valid @RequestBody ClienteDTO clienteDTO) {
        ClienteDTO updateCliente = adminClienteService.update(id, clienteDTO);
        return new ResponseEntity<ClienteDTO>(updateCliente, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Cliente> deleteCliente(@PathVariable("id") Integer id) {
        adminClienteService.delete(id);
        return new ResponseEntity<Cliente>(HttpStatus.NO_CONTENT);
    }
}

