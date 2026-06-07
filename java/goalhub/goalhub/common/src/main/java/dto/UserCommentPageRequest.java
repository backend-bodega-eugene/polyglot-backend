package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户评论分页查询请求。
 *
 * <p>用于分页查询用户自己的评论记录。</p>
 */
@Schema(description = "用户评论分页查询请求")
@Data
public class UserCommentPageRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex = 1;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;
}
