package dto;

import lombok.Data;

@Data
public class AdminContentPageRequest {

    private Integer pageIndex = 1;

    private Integer pageSize = 20;

    private String type;

    private String status;

    private String keyword;
}