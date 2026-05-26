package dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {

    private Long total;

    private Integer pageIndex;

    private Integer pageSize;

    private List<T> records;
}