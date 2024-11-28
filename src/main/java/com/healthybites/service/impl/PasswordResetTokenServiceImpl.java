package com.healthybites.service.impl;

import com.healthybites.exception.ResourceNotFoundException;
import com.healthybites.integration.notification.email.dto.Mail;
import com.healthybites.integration.notification.email.service.EmailService;
import com.healthybites.model.entity.PasswordResetToken;
import com.healthybites.model.entity.Usuario;
import com.healthybites.repository.PasswordResetTokenRepository;
import com.healthybites.repository.UsuarioRepository;
import com.healthybites.service.PasswordResetTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${spring.mail.username}")
    private String mailFrom;

    @Transactional
    @Override
    public void createAndSendPasswordResetToken(String email) throws Exception {
        Usuario usuario = usuarioRepository.findByCorreo(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Crear un token de restablecimiento de contraseña
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(UUID.randomUUID().toString());
        passwordResetToken.setUsuario(usuario); // Relacionar el token con el usuario
        passwordResetToken.setExpiration(10);

        // Guardar el token en la base de datos
        passwordResetTokenRepository.save(passwordResetToken);

        Map<String, Object> model = new HashMap<>();
        String resetUrl = "https://healthy-bites-v1.netlify.app/auth/reset-password/" + passwordResetToken.getToken();
        model.put("user", usuario.getCorreo());
        model.put("resetUrl", resetUrl);

        Mail mail = emailService.createMail(
                usuario.getCorreo(),
                "Restablecer Contraseña",
                model,
                mailFrom
        );

        emailService.sendEmail(mail,"email/password-reset-template");
    }

    @Override
    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token de restablecimiento de contraseña no encontrado"));
    }

    @Override
    public void removeResetToken(PasswordResetToken passwordResetToken) {
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    @Override
    public boolean isValidToken(String token) {
        return passwordResetTokenRepository.findByToken(token)
                .filter(t->!t.isExpired())
                .isPresent();
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .filter(t->!t.isExpired())
                .orElseThrow(() -> new ResourceNotFoundException("Token invalido o expirado"));

        Usuario usuario = resetToken.getUsuario();
        usuario.setContrasena(passwordEncoder.encode(newPassword));
        usuarioRepository.save(usuario);
        passwordResetTokenRepository.delete(resetToken);
    }
}
