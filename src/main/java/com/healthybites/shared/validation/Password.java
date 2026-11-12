package com.healthybites.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Longitud de una contraseña nueva (la obligatoriedad, con su mensaje, va en cada campo). */
@Size(min = 10, max = 64, message = "La contraseña debe tener entre 10 y 64 caracteres") @Target({
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
public @interface Password {

    String message() default "La contraseña debe tener entre 10 y 64 caracteres";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
