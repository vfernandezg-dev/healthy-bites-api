package com.healthybites.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Correo obligatorio y válido, de hasta 254 caracteres (el máximo de una dirección). */
@NotBlank(message = "El correo es obligatorio") @Email(message = "Debe ser un correo válido") @Size(max = 254, message = "Máximo 254 caracteres") @Target({
    ElementType.METHOD,
    ElementType.FIELD,
    ElementType.ANNOTATION_TYPE,
    ElementType.CONSTRUCTOR,
    ElementType.PARAMETER,
    ElementType.TYPE_USE
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {})
public @interface EmailAddress {

    String message() default "Debe ser un correo válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
