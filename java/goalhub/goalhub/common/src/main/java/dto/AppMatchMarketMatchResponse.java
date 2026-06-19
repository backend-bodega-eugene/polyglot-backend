package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * App 比赛维度赛事玩法赔率响应。
 *
 * <p>按比赛聚合玩法列表，用于前端展示单场比赛的可投注玩法。</p>
 */
@Data
@Schema(description = "App 比赛维度赛事玩法赔率响应")
public class AppMatchMarketMatchResponse {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛ID", example = "10001")
    private Long matchId;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码", example = "MATCH202606170001")
    private String matchCode;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称", example = "France vs Germany")
    private String matchName;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态", example = "NOT_STARTED")
    private String matchStatus;

    /**
     * 计划开赛时间 UTC 字符串。
     */
    @Schema(description = "计划开赛时间UTC字符串", example = "2026-06-17T12:00:00Z")
    private String scheduledStartTimeUtc;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队ID", example = "2001")
    private Long homeTeamId;

    /**
     * 主队名称。
     */
    @Schema(description = "主队名称", example = "France")
    private String homeTeamName;

    /**
     * 主队 Logo 地址。
     */
    @Schema(description = "主队Logo地址")
    private String homeTeamLogoUrl;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队ID", example = "2002")
    private Long awayTeamId;

    /**
     * 客队名称。
     */
    @Schema(description = "客队名称", example = "Germany")
    private String awayTeamName;

    /**
     * 客队 Logo 地址。
     */
    @Schema(description = "客队Logo地址")
    private String awayTeamLogoUrl;

    /**
     * 比赛下的玩法列表。
     */
    @Schema(description = "比赛下的玩法列表")
    private List<AppMatchMarketResponse> markets;
}
