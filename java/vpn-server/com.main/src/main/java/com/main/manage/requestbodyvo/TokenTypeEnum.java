package com.main.manage.requestbodyvo;

import com.common.util.IGlossary;

public enum TokenTypeEnum implements IGlossary {
    RMB("人民币"),
    POINTS("积分"),
    TRX("波长"),
    ETH("以太坊"),
    USDT("USDT"),
    KUC("KUC");

    TokenTypeEnum(String name) {
        this.name = name;
    }

    TokenTypeEnum(TokenTypeEnum parent, String name) {
        this.name = name;
        this.value = value;
        this.parent = parent;
    }

    TokenTypeEnum parent;
    private String name;
    private Integer value;

    public Integer getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

}