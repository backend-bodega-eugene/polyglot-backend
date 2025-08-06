package com.common.web.editor;


import com.common.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateTimeEditor extends PropertyEditorSupport {
    /**
     * 默认的日期格式
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private final boolean emptyAsNull;

    public LocalDateTimeEditor(boolean emptyAsNull) {
        this.emptyAsNull = emptyAsNull;
    }

    @Override
    public String getAsText() {
        if (Objects.isNull(this.getValue())) {
            return null;
        }
        return ((LocalDateTime) this.getValue()).format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (emptyAsNull && !StringUtils.hasText(text)) {
            this.setValue(null);
        } else {
            this.setValue(LocalDateTime.parse(text, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        }
    }
}