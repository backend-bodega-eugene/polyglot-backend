package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户评论回复请求。
 *
 * <p>用于后台回复指定用户评论。</p>
 */
@Schema(description = "用户评论回复请求")
@Data
public class UserCommentReplyRequest {

    /**
     * 用户评论 ID。
     */
    @Schema(description = "用户评论 ID", example = "1")
    private Long id;

    /**
     * 回复内容。
     */
    @Schema(description = "回复内容")
    private String replyContent;
}
