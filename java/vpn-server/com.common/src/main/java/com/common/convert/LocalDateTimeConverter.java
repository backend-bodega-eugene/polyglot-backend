package com.common.convert;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LocalDateTimeConverter implements Converter<String, LocalDateTime> {
    //2020-08-26%2001:02:03
    @Override
    public LocalDateTime convert(String s) {
        return LocalDateTime.parse(s, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}