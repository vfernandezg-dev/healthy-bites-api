package com.healthybites.service;

import com.healthybites.dto.PasswordResetRequestDTO;
import com.healthybites.dto.ResetPasswordDTO;

public interface AuthService {
    // Genera un token de recuperacion y lo envia por correo (nunca se devuelve en la respuesta)
    void sendPasswordResetToken(PasswordResetRequestDTO requestDTO) throws Exception;

    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}
