package dto;

import lombok.Data;

import java.util.List;

/**
 * 通用分页响应。
 *
 * @param <T> 分页记录类型
 */
@Data
public class PageResponse<T> {

    /**
     * 总记录数。
     */
    private Long total;

    /**
     * 当前页码。
     */
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    private Integer pageSize;

    /**
     * 当前页记录列表。
     */
    private List<T> records;
}
