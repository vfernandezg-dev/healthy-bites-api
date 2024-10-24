package com.healthybites.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private String token;       // El token JWT
    private String nombre;      // El nombre del usuario
    private String apellido;    // El apellido del usuario
    private String role;        // El rol del usuario
}
