package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户赛事关注响应。
 */
@Data
@Schema(description = "用户赛事关注响应")
public class UserMatchFollowResponse {

    /**
     * 关注记录 ID。
     */
    @Schema(description = "关注记录 ID")
    private Long id;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID")
    private Long userId;

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事 ID")
    private Long matchId;

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
