package com.common.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Arrays.stream;

/**
 * 指定int枚举验证器
 *
 * @author admin
 */
public final class IntTypeRangeValidator
        implements ConstraintValidator<IntTypeRange, Integer> {

    private IntTypeRange constraint;

    public void initialize(IntTypeRange constraint) {
        this.constraint = constraint;
    }

    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        var values = constraint.values();
        return values.length > 0 && value != null
                && stream(values).anyMatch(value::equals);
    }

}