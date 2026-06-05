package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户赛事关注响应。
 *
 * <p>返回用户关注赛事的关系 ID、用户 ID、赛事 ID 和维护时间。</p>
 */
@Data
@Schema(description = "用户赛事关注响应")
public class UserMatchFollowResponse {

    /**
     * 关注记录 ID。
     */
    @Schema(description = "关注记录 ID", example = "1")
    private Long id;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事 ID", example = "1001")
    private Long matchId;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间", example = "2026-06-04T12:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间", example = "2026-06-04T12:30:00")
    private LocalDateTime updatedAt;
}
