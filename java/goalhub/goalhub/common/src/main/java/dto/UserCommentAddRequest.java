package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户评论新增请求。
 *
 * <p>用于用户提交意见反馈、联系方式和评论内容。</p>
 */
@Schema(description = "用户评论新增请求")
@Data
public class UserCommentAddRequest {

    /**
     * 联系方式。
     */
    @Schema(description = "联系方式")
    private String contact;

    /**
     * 评论内容。
     */
    @Schema(description = "评论内容")
    private String message;
}
