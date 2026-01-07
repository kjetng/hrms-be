package org.httt2.hrms.bonus.validation.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = org.httt2.hrms.bonus.validator.MultipleOfValidator.class)
@Target({ FIELD, METHOD, PARAMETER })
@Retention(RUNTIME)
public @interface MultipleOf {

    String message() default "Value must be a multiple of {value}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    long value();
}
