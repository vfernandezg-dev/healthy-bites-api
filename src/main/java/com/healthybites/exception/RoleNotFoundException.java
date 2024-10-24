package com.healthybites.exception;

public class RoleNotFoundException extends RuntimeException {

  public RoleNotFoundException() {
    super("Rol no encontrado para el usuario.");
  }

  public RoleNotFoundException(String message) {
    super(message);
  }
}