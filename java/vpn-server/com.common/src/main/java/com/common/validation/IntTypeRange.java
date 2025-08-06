package com.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 指定int集合验证注解
 *
 * @author admin
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = IntTypeRangeValidator.class)
public @interface IntTypeRange {

    String message() default "不在指定范围内";

    int[] values() default {};

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}