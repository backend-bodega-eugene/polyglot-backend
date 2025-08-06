package com.common.validation;

import com.common.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static java.util.Arrays.stream;

/**
 * 指定字符串枚举验证器
 *
 * @author admin
 */
public final class StrTypeRangeValidator
        implements ConstraintValidator<StrTypeRange, String> {

    private StrTypeRange constraint;

    public void initialize(StrTypeRange constraint) {
        this.constraint = constraint;
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        var values = constraint.values();
        return values.length > 0 && StringUtils.isNotEmpty(value)
                && stream(values).anyMatch(value::equals);
    }

}