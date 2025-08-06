package com.common.web.editor;


import com.common.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class LocalDateEditor extends PropertyEditorSupport {
    /**
     * 默认的日期格式
     */
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private final boolean emptyAsNull;

    public LocalDateEditor(boolean emptyAsNull) {
        this.emptyAsNull = emptyAsNull;
    }

    @Override
    public String getAsText() {
        if (Objects.isNull(this.getValue())) {
            return null;
        }
        return ((LocalDate) this.getValue()).format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (emptyAsNull && !StringUtils.hasText(text)) {
            this.setValue(null);
        } else {
            this.setValue(LocalDate.parse(text, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        }
    }
}