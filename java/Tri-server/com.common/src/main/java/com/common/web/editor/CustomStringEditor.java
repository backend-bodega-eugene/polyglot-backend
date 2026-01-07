//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.web.editor;


import com.common.util.StringUtils;

import java.beans.PropertyEditorSupport;

public class CustomStringEditor extends PropertyEditorSupport {

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
        return super.getAsText();
    }
}