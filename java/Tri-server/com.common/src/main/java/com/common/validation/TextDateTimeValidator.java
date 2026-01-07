package com.common.validation;

import com.common.util.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.format.DateTimeFormatter.ofPattern;

/**
 * 时间字符验证器
 *
 * @author admin
 */
public final class TextDateTimeValidator
        implements ConstraintValidator<TextDateTime, String> {

    private static final ConcurrentHashMap<String, DateTimeFormatter> FORMATS = new ConcurrentHashMap<>();

    private TextDateTime constraint;

    public void initialize(TextDateTime constraint) {
        this.constraint = constraint;
    }

    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.isNotEmpty(value) && validate(value);
    }

    private boolean validate(String value) {
        try {
            return doWork(value);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private boolean doWork(String value) {
        var key = constraint.format();
        if (FORMATS.get(key) != null) return true;
        if (build(key) != null) return true;
        return false;
    }

    private DateTimeFormatter build(String key) {
        synchronized (FORMATS) {
            var format = FORMATS.get(key);
            if (format == null) FORMATS.put(key, format = ofPattern(key));
            return format;
        }
    }
}