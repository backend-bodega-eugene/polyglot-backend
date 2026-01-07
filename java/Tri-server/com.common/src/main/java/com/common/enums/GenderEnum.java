//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.enums;

import com.common.util.IGlossary;

public enum GenderEnum implements IGlossary {
    Male("男"),
    Female("女");

    private String name;

    GenderEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
