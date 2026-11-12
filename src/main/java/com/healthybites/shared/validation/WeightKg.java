package com.healthybites.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Peso corporal en kilos: entre 20 y 400, con un decimal como mucho. */
@DecimalMin(value = "20", message = "El peso mínimo es 20 kg") @DecimalMax(value = "400", message = "El peso máximo es 400 kg") @Digits(integer = 3, fraction = 1, message = "Como mucho un decimal") @Target({
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
public @interface WeightKg {

    String message() default "Peso no válido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
