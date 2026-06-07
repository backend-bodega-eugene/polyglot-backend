package dto;

import lombok.Data;

@Data
public class UserCommentPageRequest {

    private Integer pageIndex = 1;

    private Integer pageSize = 20;
}