package com.healthybites.api;

import com.healthybites.model.entity.Cliente;
import com.healthybites.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final ClienteService clienteService;

    @PostMapping("/register")
    public ResponseEntity<Cliente> register(@RequestBody Cliente cliente){
        Cliente newCliente = clienteService.registerCliente(cliente);
        return new ResponseEntity<>(newCliente, HttpStatus.CREATED);
    }
}


