package com.healthybites.service;

import com.healthybites.dto.UserProfileDTO;
import com.healthybites.dto.UserRegistrationDTO;

public interface UsuarioService {
    // Registrar un cliente
    UserProfileDTO registrarCliente(UserRegistrationDTO registrationDTO);

    // Registrar un nutricionista
    UserProfileDTO registrarNutricionista(UserRegistrationDTO registrationDTO);

    // Actualizar el perfil de un usuario
    UserProfileDTO updateProfile(Integer id, UserProfileDTO profileDTO);

    // Obtener el perfil de un usuario
    UserProfileDTO getUserProfileById(Integer id);
}
