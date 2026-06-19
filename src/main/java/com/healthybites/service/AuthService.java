package com.healthybites.service;

import com.healthybites.dto.ResetPasswordDTO;
import com.healthybites.dto.PasswordResetRequestDTO;

public interface AuthService {
    String generatePasswordResetToken(PasswordResetRequestDTO requestDTO);
    void resetPassword(ResetPasswordDTO resetPasswordDTO);
}