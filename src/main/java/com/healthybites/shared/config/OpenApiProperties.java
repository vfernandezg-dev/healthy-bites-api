package com.healthybites.shared.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Datos del contrato OpenAPI ({@code healthybites.openapi.*}). */
@ConfigurationProperties("healthybites.openapi")
@Validated
public record OpenApiProperties(@NotBlank String contactEmail) {}
