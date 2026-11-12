package com.healthybites.dto;

import lombok.Data;

/**
 * Respuesta de login y de refresh. El access token es de vida corta y el web-app lo guarda solo en memoria; el
 * refresh token nunca va en el cuerpo, solo en una cookie httpOnly.
 */
@Data
public class AuthResponseDTO {
    private String accessToken;
    private String tokenType;
    /** Segundos de validez del access token. */
    private long expiresIn;

    private Integer id;         // El id del usuario
    private String nombre;      // El nombre del usuario
    private String apellido;    // El apellido del usuario
    private String role;        // El rol del usuario
    private Integer clienteId;  // El id del cliente (si es cliente)
    private Integer nutricionistaId; // El id del nutricionista (si es nutricionista)
}
