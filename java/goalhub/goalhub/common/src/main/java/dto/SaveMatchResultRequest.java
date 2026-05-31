package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 保存赛事结果请求。
 */
@Data
@Schema(description = "保存赛事结果请求")
public class SaveMatchResultRequest {

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事ID")
    private Long matchId;

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
     * 比赛结束时间。
     */
    @Schema(description = "比赛结束时间")
    private LocalDateTime matchEndedAt;

    /**
     * 主队点球次数。
     */
    @Schema(description = "主队点球次数")
    private Integer homePenaltyCount;

    /**
     * 客队点球次数。
     */
    @Schema(description = "客队点球次数")
    private Integer awayPenaltyCount;

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
     * 主队界外球次数。
     */
    @Schema(description = "主队界外球次数")
    private Integer homeThrowInCount;

    /**
     * 客队界外球次数。
     */
    @Schema(description = "客队界外球次数")
    private Integer awayThrowInCount;

    /**
     * 主队犯规次数。
     */
    @Schema(description = "主队犯规次数")
    private Integer homeFoulCount;

    /**
     * 客队犯规次数。
     */
    @Schema(description = "客队犯规次数")
    private Integer awayFoulCount;

    /**
     * 主队任意球次数。
     */
    @Schema(description = "主队任意球次数")
    private Integer homeFreeKickCount;

    /**
     * 客队任意球次数。
     */
    @Schema(description = "客队任意球次数")
    private Integer awayFreeKickCount;

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
