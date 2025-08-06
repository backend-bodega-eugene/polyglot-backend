package com.common.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 时间字符串验证注解
 *
 * @author admin
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Constraint(validatedBy = TextDateTimeValidator.class)
public @interface TextDateTime {

    String message() default "错误的时间格式";

    String format() default "yyyy-MM-dd";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}