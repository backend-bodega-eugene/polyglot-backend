//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.web.editor;

import com.common.util.Money;
import com.common.util.StringUtils;

import java.beans.PropertyEditorSupport;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class CustomMoneyEditor extends PropertyEditorSupport {
    public void setAsText(String text) throws IllegalArgumentException {
        if (!StringUtils.hasText(text)) {
            this.setValue((Object) null);
        } else {
            this.setValue(new Money(new BigDecimal(text)));
        }
    }

    public String getAsText() {
        Money money = (Money) this.getValue();
        if (money != null) {
            new DecimalFormat(",###.##");
            return String.valueOf(money.getAmount());
        } else {
            return super.getAsText();
        }
    }
}