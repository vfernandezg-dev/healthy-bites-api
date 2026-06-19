package com.healthybites.mapper;

import com.healthybites.dto.AuthResponseDTO;
import com.healthybites.dto.LoginDTO;
import com.healthybites.dto.UserProfileDTO;
import com.healthybites.dto.UserRegistrationDTO;
import com.healthybites.model.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UsuarioMapper {

    private final ModelMapper modelMapper;

    public Usuario toUserEntity(UserRegistrationDTO registrationDTO) {
        return modelMapper.map(registrationDTO, Usuario.class);
    }

    public UserProfileDTO toUserProfileDTO(Usuario user) {
        UserProfileDTO userProfileDTO = modelMapper.map(user, UserProfileDTO.class);

        if (user.getNutricionista() != null) {
            userProfileDTO.setNombre(user.getNutricionista().getNombre());
            userProfileDTO.setApellido(user.getNutricionista().getApellido());
            userProfileDTO.setBio(user.getNutricionista().getBio());
        }

        if (user.getCliente() != null) {
            userProfileDTO.setNombre(user.getCliente().getNombre());
            userProfileDTO.setApellido(user.getCliente().getApellido());
            userProfileDTO.setSexo(user.getCliente().getSexo());
            userProfileDTO.setEdad(user.getCliente().getEdad());
            userProfileDTO.setAltura(user.getCliente().getAltura());
            userProfileDTO.setPeso(user.getCliente().getPeso());
        }

        return userProfileDTO;
    }

    public Usuario toUserEntity(LoginDTO loginDTO) {
        return modelMapper.map(loginDTO, Usuario.class);
    }

    public AuthResponseDTO toAuthResponseDTO(Usuario user, String token) {
        AuthResponseDTO authResponseDTO = new AuthResponseDTO();
        authResponseDTO.setToken(token); // Asignar el token JWT

        // Obtener el nombre y apellido del usuario
        String nombre = (user.getNutricionista() != null) ? user.getNutricionista().getNombre()
                : (user.getCliente() != null) ? user.getCliente().getNombre()
                : "Admin";

        String apellido = (user.getNutricionista() != null) ? user.getNutricionista().getApellido()
                : (user.getCliente() != null) ? user.getCliente().getApellido()
                : "User";

        authResponseDTO.setNombre(nombre);
        authResponseDTO.setApellido(apellido);

        authResponseDTO.setRole(user.getRol().getNombre().name()); // Obtener el rol del usuario

        return authResponseDTO;
    }
}
