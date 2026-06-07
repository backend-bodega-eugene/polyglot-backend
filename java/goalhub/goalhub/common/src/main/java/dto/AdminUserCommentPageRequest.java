package dto;

import lombok.Data;

@Data
public class AdminUserCommentPageRequest {

    private Integer pageIndex = 1;

    private Integer pageSize = 20;

    private Long userId;
}