package com.healthybites.mapper;

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
}
