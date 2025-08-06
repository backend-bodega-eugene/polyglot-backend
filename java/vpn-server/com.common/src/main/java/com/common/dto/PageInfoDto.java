package com.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;

@Data
public class PageInfoDto implements Serializable {
    /**
     * 页索引
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;
    /**
     * 排序列
     */
    private String property = "id";
    /**
     * 排序类型
     */
    private Sort.Direction direction = Sort.Direction.DESC;

    @JsonIgnore
    public Pageable getPageable() {
        return PageRequest.of(page, size, direction, property);
    }
}