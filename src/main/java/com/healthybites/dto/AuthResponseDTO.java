package com.healthybites.dto;

import lombok.Data;

@Data
public class AuthResponseDTO {
    private Integer id;         // El id del usuario
    private String token;       // El token JWT
    private String nombre;      // El nombre del usuario
    private String apellido;    // El apellido del usuario
    private String role;        // El rol del usuario

    private Integer clienteId;  // El id del cliente (si es cliente)
    private Integer nutricionistaId; // El id del nutricionista (si es nutricionista)
}
