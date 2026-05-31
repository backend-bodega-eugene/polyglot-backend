package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 足球比赛详情响应。
 */
@Schema(description = "足球比赛详情响应")
@Data
public class SoccerMatchDetailResponse {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1")
    private Long id;

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    private Long leagueId;

    /**
     * 联赛名称。
     */
    @Schema(description = "联赛名称", example = "FIFA World Cup 2026")
    private String leagueName;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码", example = "FIFA_WORLD_CUP_2026_FINAL")
    private String matchCode;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称", example = "Brazil vs Argentina")
    private String matchName;

    /**
     * 阶段编码。
     */
    @Schema(description = "阶段编码", example = "FINAL")
    private String stageCode;

    /**
     * 阶段名称。
     */
    @Schema(description = "阶段名称", example = "Final")
    private String stageName;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队 ID", example = "1001")
    private Long homeTeamId;

    /**
     * 主队名称。
     */
    @Schema(description = "主队名称", example = "Brazil")
    private String homeTeamName;

    /**
     * 主队简称。
     */
    @Schema(description = "主队简称", example = "BRA")
    private String homeTeamShortName;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队 ID", example = "1002")
    private Long awayTeamId;

    /**
     * 客队名称。
     */
    @Schema(description = "客队名称", example = "Argentina")
    private String awayTeamName;

    /**
     * 客队简称。
     */
    @Schema(description = "客队简称", example = "ARG")
    private String awayTeamShortName;

    /**
     * 计划开赛时间，UTC 时间字符串。
     */
    @Schema(description = "计划开赛时间，UTC 时间字符串", example = "2026-06-15T20:00:00Z")
    private String scheduledStartTimeUtc;

    /**
     * 实际开赛时间，UTC 时间字符串。
     */
    @Schema(description = "实际开赛时间，UTC 时间字符串", example = "2026-06-15T20:05:00Z")
    private String actualStartTimeUtc;

    /**
     * 实际结束时间，UTC 时间字符串。
     */
    @Schema(description = "实际结束时间，UTC 时间字符串", example = "2026-06-15T22:00:00Z")
    private String actualEndTimeUtc;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家", example = "United States")
    private String hostCountry;

    /**
     * 比赛城市。
     */
    @Schema(description = "比赛城市", example = "New York")
    private String city;

    /**
     * 比赛场馆。
     */
    @Schema(description = "比赛场馆", example = "MetLife Stadium")
    private String venue;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态", example = "SCHEDULED")
    private String status;
}
