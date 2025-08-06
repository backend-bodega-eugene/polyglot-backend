package com.main.manage.requestbodyvo;

import com.common.util.IGlossary;

public enum ChainTypeEnum implements IGlossary {
    ETH("以太坊"),
    TRON("波场"),
    HC30("内部");
    private String name;

    ChainTypeEnum(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}