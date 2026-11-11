package com.healthybites.service.impl;

import com.healthybites.dto.PasswordResetRequestDTO;
import com.healthybites.dto.ResetPasswordDTO;
import com.healthybites.service.AuthService;
import com.healthybites.service.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// Fachada sobre PasswordResetTokenService: reutiliza el token persistido, su expiracion,
// el envio de correo y el cifrado BCrypt de la nueva contrasena.
@RequiredArgsConstructor
@Service
public class AuthServiceImpl implements AuthService {

    private final PasswordResetTokenService passwordResetTokenService;

    @Override
    public void sendPasswordResetToken(PasswordResetRequestDTO requestDTO) throws Exception {
        passwordResetTokenService.createAndSendPasswordResetToken(requestDTO.getCorreo());
    }

    @Override
    public void resetPassword(ResetPasswordDTO resetPasswordDTO) {
        passwordResetTokenService.resetPassword(resetPasswordDTO.getToken(), resetPasswordDTO.getNewPassword());
    }
}
