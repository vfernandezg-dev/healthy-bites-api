package com.healthybites.api;

import com.healthybites.dto.LoginRequest;
import com.healthybites.model.entity.Cliente;
import com.healthybites.service.ClienteService;
import com.healthybites.dto.PasswordResetRequestDTO;
import com.healthybites.dto.ResetPasswordDTO;
import com.healthybites.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ClienteService clienteService;

    @PostMapping("/register")
    public ResponseEntity<Cliente> register(@RequestBody Cliente cliente) {
        Cliente newCliente = clienteService.registerCliente(cliente);
        return new ResponseEntity<>(newCliente, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Cliente> login(@RequestBody LoginRequest loginRequest) {
        Cliente cliente = clienteService.loginCliente(loginRequest);
        return new ResponseEntity<>(cliente, HttpStatus.OK);
    }

    private final AuthService authService;

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody PasswordResetRequestDTO requestDTO) {
        String token = authService.generatePasswordResetToken(requestDTO);
        return new ResponseEntity<>(token, HttpStatus.OK);  // Devolver el token en el cuerpo de la respuesta.
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetPasswordDTO resetPasswordDTO) {
        authService.resetPassword(resetPasswordDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
