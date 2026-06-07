package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前端赛事赛果响应。
 *
 * <p>返回前端展示赛事赛果所需的比赛、球队、比分和技术统计信息。</p>
 */
@Data
@Schema(description = "前端赛事赛果响应")
public class AppMatchResultResponse {

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事 ID", example = "1")
    private Long matchId;

    /**
     * 赛事编码。
     */
    @Schema(description = "赛事编码", example = "MATCH_20260604_001")
    private String matchCode;

    /**
     * 赛事名称。
     */
    @Schema(description = "赛事名称")
    private String matchName;

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    private Long leagueId;

    /**
     * 联赛名称。
     */
    @Schema(description = "联赛名称")
    private String leagueName;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队ID")
    private Long homeTeamId;

    /**
     * 主队名称。
     */
    @Schema(description = "主队名称")
    private String homeTeamName;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队ID")
    private Long awayTeamId;

    /**
     * 客队名称。
     */
    @Schema(description = "客队名称")
    private String awayTeamName;

    /**
     * 赛事状态。
     */
    @Schema(description = "赛事状态")
    private String matchStatus;

    /**
     * 赛事开始时间。
     */
    @Schema(description = "赛事开始时间")
    private LocalDateTime matchStartTime;

    /**
     * 比赛结束时间。
     */
    @Schema(description = "比赛结束时间")
    private LocalDateTime matchEndedAt;

    /**
     * 常规时间主队得分。
     */
    @Schema(description = "常规时间主队得分")
    private Integer regularHomeScore;

    /**
     * 常规时间客队得分。
     */
    @Schema(description = "常规时间客队得分")
    private Integer regularAwayScore;

    /**
     * 加时赛主队得分。
     */
    @Schema(description = "加时赛主队得分")
    private Integer extraHomeScore;

    /**
     * 加时赛客队得分。
     */
    @Schema(description = "加时赛客队得分")
    private Integer extraAwayScore;

    /**
     * 点球大战主队得分。
     */
    @Schema(description = "点球大战主队得分")
    private Integer penaltyHomeScore;

    /**
     * 点球大战客队得分。
     */
    @Schema(description = "点球大战客队得分")
    private Integer penaltyAwayScore;

    /**
     * 主队角球次数。
     */
    @Schema(description = "主队角球次数")
    private Integer homeCornerCount;

    /**
     * 客队角球次数。
     */
    @Schema(description = "客队角球次数")
    private Integer awayCornerCount;

    /**
     * 主队红牌数量。
     */
    @Schema(description = "主队红牌数量")
    private Integer homeRedCardCount;

    /**
     * 客队红牌数量。
     */
    @Schema(description = "客队红牌数量")
    private Integer awayRedCardCount;

    /**
     * 主队黄牌数量。
     */
    @Schema(description = "主队黄牌数量")
    private Integer homeYellowCardCount;

    /**
     * 客队黄牌数量。
     */
    @Schema(description = "客队黄牌数量")
    private Integer awayYellowCardCount;
}
