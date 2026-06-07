package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户评论响应。
 *
 * <p>返回用户评论、联系方式、后台回复和时间信息。</p>
 */
@Schema(description = "用户评论响应")
@Data
public class UserCommentResponse {

    /**
     * 用户评论 ID。
     */
    @Schema(description = "用户评论 ID", example = "1")
    private Long id;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

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

    /**
     * 回复内容。
     */
    @Schema(description = "回复内容")
    private String replyContent;

    /**
     * 回复时间。
     */
    @Schema(description = "回复时间")
    private LocalDateTime replyTime;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
