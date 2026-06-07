package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户评论详情查询请求。
 *
 * <p>用于按评论 ID 查询用户评论详情。</p>
 */
@Schema(description = "用户评论详情查询请求")
@Data
public class UserCommentDetailRequest {

    /**
     * 用户评论 ID。
     */
    @Schema(description = "用户评论 ID", example = "1")
    private Long id;
}
