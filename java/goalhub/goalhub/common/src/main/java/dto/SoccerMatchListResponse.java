package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 足球比赛列表响应。
 */
@Schema(description = "足球比赛列表响应")
@Data
public class SoccerMatchListResponse {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1")
    private Long id;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称", example = "Brazil vs Argentina")
    private String matchName;

    /**
     * 主队名称。
     */
    @Schema(description = "主队名称", example = "Brazil")
    private String homeTeamName;

    /**
     * 客队名称。
     */
    @Schema(description = "客队名称", example = "Argentina")
    private String awayTeamName;

    /**
     * 计划开赛时间，UTC 时间字符串。
     */
    @Schema(description = "计划开赛时间，UTC 时间字符串", example = "2026-06-15T20:00:00Z")
    private String scheduledStartTimeUtc;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态", example = "SCHEDULED")
    private String status;
}
