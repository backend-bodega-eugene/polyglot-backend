package com.common.rpc;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class RpcPage<T> extends PageImpl<T> {

    public RpcPage(List<T> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public RpcPage(List<T> content) {
        super(content);
    }
}
