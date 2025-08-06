package com.common.web.editor;

import com.common.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.util.Objects;

public class StringEditor extends PropertyEditorSupport {
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            this.setValue(text.strip());
        } else {
            this.setValue(null);
        }
    }

    @Override
    public String getAsText() {
        return Objects.isNull(this.getValue()) ? null : this.getValue().toString().strip();
    }
}