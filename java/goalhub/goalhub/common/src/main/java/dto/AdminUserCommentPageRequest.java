package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台用户评论分页查询请求。
 *
 * <p>用于后台按用户分页查询用户评论。</p>
 */
@Schema(description = "后台用户评论分页查询请求")
@Data
public class AdminUserCommentPageRequest {

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

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    private Long userId;
}
