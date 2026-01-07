package com.common.redis;

import lombok.Data;

/**
 * @author liuyazhuang
 * @version 1.0.0
 * @date 2018/8/15 23:15
 * @description FastJsonWraper包装类
 */
@Data
public class FastJsonWrapper<T> {
    private T value;

    public FastJsonWrapper() {
    }

    public FastJsonWrapper(T value) {
        this.value = value;
    }
}