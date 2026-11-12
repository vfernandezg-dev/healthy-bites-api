package com.healthybites.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Nombre o apellido de una persona (la obligatoriedad, con su mensaje, va en cada campo). */
@Size(max = 60, message = "Máximo 60 caracteres") @Target({
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
public @interface PersonName {

    String message() default "Máximo 60 caracteres";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
