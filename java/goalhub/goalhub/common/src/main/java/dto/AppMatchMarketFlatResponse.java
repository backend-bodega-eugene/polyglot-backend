package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * App 赛事玩法赔率扁平响应。
 *
 * <p>用于承载联赛、比赛、玩法和投注选项维度展开后的赔率数据。</p>
 */
@Data
@Schema(description = "App 赛事玩法赔率扁平响应")
public class AppMatchMarketFlatResponse {

    /**
     * 联赛或杯赛 ID。
     */
    @Schema(description = "联赛/杯赛ID", example = "1")
    private Long leagueId;

    /**
     * 联赛或杯赛名称。
     */
    @Schema(description = "联赛/杯赛名称", example = "世界杯")
    private String leagueName;

    /**
     * 联赛或杯赛 Logo 地址。
     */
    @Schema(description = "联赛/杯赛Logo地址")
    private String leagueLogoUrl;

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
     * 玩法 ID。
     */
    @Schema(description = "玩法ID", example = "3001")
    private Long marketId;

    /**
     * 玩法编码。
     */
    @Schema(description = "玩法编码", example = "WIN_DRAW_LOSE")
    private String marketCode;

    /**
     * 玩法名称。
     */
    @Schema(description = "玩法名称", example = "胜平负")
    private String marketName;

    /**
     * 玩法选项 ID。
     */
    @Schema(description = "玩法选项ID", example = "4001")
    private Long optionId;

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率ID", example = "5001")
    private Long marketOptionId;

    /**
     * 玩法选项编码。
     */
    @Schema(description = "玩法选项编码", example = "HOME_WIN")
    private String marketOptionCode;

    /**
     * 玩法选项名称。
     */
    @Schema(description = "玩法选项名称", example = "主胜")
    private String marketOptionName;

    /**
     * 赔率。
     */
    @Schema(description = "赔率", example = "1.95")
    private BigDecimal odds;

    /**
     * 下注状态。
     */
    @Schema(description = "下注状态", example = "OPEN")
    private String betStatus;

    /**
     * 排序值。
     */
    @Schema(description = "排序值，数值越小越靠前", example = "10")
    private Integer sortOrder;
}
