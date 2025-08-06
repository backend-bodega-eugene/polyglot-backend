//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class KeyValueDto<K, V> implements Serializable {
    private K key;
    private V value;

    public static KeyValueDto<String, Integer> instance(String key, Integer value) {
        KeyValueDto keyValue = new KeyValueDto();
        keyValue.setKey(key);
        keyValue.setValue(value);
        return keyValue;
    }
}
