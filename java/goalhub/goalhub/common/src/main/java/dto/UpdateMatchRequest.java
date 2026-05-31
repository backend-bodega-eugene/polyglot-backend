package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台更新比赛请求。
 */
@Schema(description = "后台更新比赛请求")
@Data
public class UpdateMatchRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID")
    private Long id;

    /**
     * 所属联赛 ID。
     */
    @Schema(description = "所属联赛 ID")
    private Long leagueId;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码")
    private String matchCode;

    /**
     * 阶段编码。
     */
    @Schema(description = "阶段编码")
    private String stageCode;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队 ID")
    private Long homeTeamId;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队 ID")
    private Long awayTeamId;

    /**
     * 计划开赛时间，UTC 时间。
     */
    @Schema(description = "计划开赛时间，UTC 时间")
    private LocalDateTime scheduledStartTimeUtc;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家")
    private String hostCountry;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态")
    private String status;
}
