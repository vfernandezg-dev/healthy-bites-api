package com.healthybites.service.impl;

import com.healthybites.model.entity.Usuario;
import com.healthybites.repository.UsuarioRepository;
import com.healthybites.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    @Override
    public Usuario registerUsuario(Usuario usuario) {
        if(usuarioRepository.existsByCorreo(usuario.getCorreo())) {
            throw new RuntimeException("El email ya esta registrado");
        }
        usuario.setFechaCreacion(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }
}
