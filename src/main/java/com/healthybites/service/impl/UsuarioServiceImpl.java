package com.healthybites.service.impl;

import com.healthybites.dto.AuthResponseDTO;
import com.healthybites.dto.LoginDTO;
import com.healthybites.dto.UserProfileDTO;
import com.healthybites.dto.UserRegistrationDTO;
import com.healthybites.mapper.UsuarioMapper;
import com.healthybites.model.entity.Cliente;
import com.healthybites.model.entity.Nutricionista;
import com.healthybites.model.entity.Rol;
import com.healthybites.model.entity.Usuario;
import com.healthybites.model.enums.ERol;
import com.healthybites.repository.ClienteRepository;
import com.healthybites.repository.NutricionistaRepository;
import com.healthybites.repository.RoleRepository;
import com.healthybites.repository.UsuarioRepository;
import com.healthybites.security.UserPrincipal;
import com.healthybites.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final NutricionistaRepository nutricionistaRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioMapper usuarioMapper;

    private final AuthenticationManager authenticationManager;

    @Override
    public UserProfileDTO registrarCliente(UserRegistrationDTO registrationDTO) {
        return registrarUserWithRole(registrationDTO, ERol.CLIENTE);
    }

    @Override
    public UserProfileDTO registrarNutricionista(UserRegistrationDTO registrationDTO) {
        return registrarUserWithRole(registrationDTO, ERol.NUTRICIONISTA);
    }

    @Override
    public UserProfileDTO updateProfile(Integer id, UserProfileDTO profileDTO) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        // Verificar si existe un cliente o nutricionista con el mismo nombre y apellido
        boolean existAsCliente = clienteRepository.existsByNombreAndApellidoAndUsuarioIdNot(
                profileDTO.getNombre(), profileDTO.getApellido(), id);
        boolean existAsNutricionista = nutricionistaRepository.existsByNombreAndApellidoAndUsuarioIdNot(
                profileDTO.getNombre(), profileDTO.getApellido(), id);

        if (existAsCliente || existAsNutricionista) {
            throw new IllegalArgumentException("Ya existe un usuario con el mismo nombre y apellido");
        }

        if (user.getCliente() != null) {
            Cliente cliente = user.getCliente();
            cliente.setNombre(profileDTO.getNombre());
            cliente.setApellido(profileDTO.getApellido());
            cliente.setSexo(profileDTO.getSexo());
            cliente.setEdad(profileDTO.getEdad());
            cliente.setAltura(profileDTO.getAltura());
            cliente.setPeso(profileDTO.getPeso());
            cliente.setFechaActualizacion(LocalDateTime.now());
        } else if (user.getNutricionista() != null) {
            Nutricionista nutricionista = user.getNutricionista();
            nutricionista.setNombre(profileDTO.getNombre());
            nutricionista.setApellido(profileDTO.getApellido());
            nutricionista.setBio(profileDTO.getBio());
            nutricionista.setFechaActualizacion(LocalDateTime.now());
        }

        Usuario savedUser = usuarioRepository.save(user);

        return usuarioMapper.toUserProfileDTO(savedUser);
    }

    @Override
    public AuthResponseDTO login(LoginDTO loginDTO) {
        // Autenticar al usuario utilizando AuthenticationManager
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDTO.getCorreo(), loginDTO.getContrasena())
        );

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Usuario user = userPrincipal.getUsuario();

        String token = ""; // Generar un token JWT

        AuthResponseDTO authResponseDTO = usuarioMapper.toAuthResponseDTO(user, token);

        return null;
    }

    @Override
    public UserProfileDTO getUserProfileById(Integer id) {
        Usuario user = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        return usuarioMapper.toUserProfileDTO(user);
    }

    private UserProfileDTO registrarUserWithRole(UserRegistrationDTO registrationDTO, ERol rolEnum) {
        // Verificar si el email ya está registrado o si ya existe un usuario con el mismo nombre y apellido
        boolean existsByEmail = usuarioRepository.existsByCorreo(registrationDTO.getCorreo());
        boolean existAsCliente = clienteRepository.existsByNombreAndApellido(registrationDTO.getNombre(), registrationDTO.getApellido());
        boolean existAsNutricionista = nutricionistaRepository.existsByNombreAndApellido(registrationDTO.getNombre(), registrationDTO.getApellido());

        if (existsByEmail) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if (existAsCliente || existAsNutricionista) {
            throw new IllegalArgumentException("Ya existe un usuario con el mismo nombre y apellido");
        }

        Rol rol = roleRepository.findByNombre(rolEnum)
                .orElseThrow(() -> new RuntimeException("Error: Rol no encontrado."));

        registrationDTO.setContrasena(passwordEncoder.encode(registrationDTO.getContrasena()));

        Usuario user = usuarioMapper.toUserEntity(registrationDTO);
        user.setRol(rol);

        if (rolEnum.equals(ERol.CLIENTE)) {
            Cliente cliente = new Cliente();
            cliente.setNombre(registrationDTO.getNombre());
            cliente.setApellido(registrationDTO.getApellido());
            cliente.setSexo(registrationDTO.getSexo());
            cliente.setEdad(registrationDTO.getEdad());
            cliente.setAltura(registrationDTO.getAltura());
            cliente.setPeso(registrationDTO.getPeso());
            cliente.setFechaCreacion(LocalDateTime.now());
            cliente.setFechaActualizacion(LocalDateTime.now());
            cliente.setUsuario(user);
            user.setCliente(cliente);
        } else if (rolEnum.equals(ERol.NUTRICIONISTA)) {
            Nutricionista nutricionista = new Nutricionista();
            nutricionista.setNombre(registrationDTO.getNombre());
            nutricionista.setApellido(registrationDTO.getApellido());
            nutricionista.setBio(registrationDTO.getBio());
            nutricionista.setFechaCreacion(LocalDateTime.now());
            nutricionista.setFechaActualizacion(LocalDateTime.now());
            nutricionista.setUsuario(user);
            user.setNutricionista(nutricionista);
        }

        Usuario savedUser = usuarioRepository.save(user);

        return usuarioMapper.toUserProfileDTO(savedUser);
    }
}
