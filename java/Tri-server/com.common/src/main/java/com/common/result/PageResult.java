package com.common.result;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.io.Serializable;
import java.util.List;

/**
 * 分页返回对象
 *
 * @param <T>
 */
@Data
public class PageResult<T> implements Serializable {
    private Integer pageSize;
    private Long totalCount;
    private Integer totalPage;
    private Integer pageIndex;
    private List<T> list;

    public static PageResult convert(Page page) {
        var result = new PageResult<>();
        result.setPageSize(page.getSize());
        result.setTotalCount(page.getTotalElements());
        result.setTotalPage(page.getTotalPages());
        result.setPageIndex(page.getNumber() + 1);
        result.setList(page.getContent());
        return result;
    }
}
