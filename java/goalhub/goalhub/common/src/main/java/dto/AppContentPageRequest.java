package dto;

import lombok.Data;

@Data
public class AppContentPageRequest {

    private Integer pageIndex = 1;

    private Integer pageSize = 20;
}