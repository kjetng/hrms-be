package org.httt2.hrms.bonus.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.httt2.hrms.bonus.validation.annotation.MultipleOf;


public class MultipleOfValidator
        implements ConstraintValidator<MultipleOf, Number> {

    private long divisor;

    @Override
    public void initialize(MultipleOf annotation) {
        this.divisor = annotation.value();
    }

    @Override
    public boolean isValid(Number value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // handled by @NotNull
        }
        return value.longValue() % divisor == 0;
    }
}
