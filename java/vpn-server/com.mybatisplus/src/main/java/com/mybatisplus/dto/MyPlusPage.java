package com.mybatisplus.dto;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public class MyPlusPage<T> extends Page<T> {

    public MyPlusPage(long current, long size) {
        super(current, size);
    }

}
