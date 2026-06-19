package com.healthybites.service.impl;

import com.healthybites.dto.ResetPasswordDTO;
import com.healthybites.dto.PasswordResetRequestDTO;
import com.healthybites.exception.BadRequestException;
import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.model.entity.Cliente;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {
    private final ClienteRepository clienteRepository;

    @Override
    @Transactional
    public String generatePasswordResetToken(PasswordResetRequestDTO requestDTO) {
        Cliente cliente = clienteRepository.findByCorreo(requestDTO.getCorreo())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un cliente con ese correo"));

        String token = UUID.randomUUID().toString();
        cliente.setResetPasswordToken(token);
        clienteRepository.save(cliente);

        return token;  // Devuelve el token en lugar de solo confirmar la operación.
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        Cliente cliente = clienteRepository.findByResetPasswordToken(resetPasswordDTO.getToken())
                .orElseThrow(() -> new BadRequestException("Token inválido"));

        cliente.setContrasena(resetPasswordDTO.getNewPassword());
        cliente.setResetPasswordToken(null);  // Limpiar el token después de restablecer la contraseña
        clienteRepository.save(cliente);
    }
}
