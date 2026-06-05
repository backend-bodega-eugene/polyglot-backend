package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 保存赛事结果请求。
 *
 * <p>用于录入或更新比赛比分、结束时间、点球、角球、牌数等赛果统计数据。</p>
 */
@Data
@Schema(description = "保存赛事结果请求")
public class SaveMatchResultRequest {

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事ID", example = "1001")
    @NotNull(message = "parameter.error")
    private Long matchId;

    /**
     * 常规时间主队得分。
     */
    @Schema(description = "常规时间主队得分", example = "2")
    @Min(value = 0, message = "parameter.error")
    private Integer regularHomeScore;

    /**
     * 常规时间客队得分。
     */
    @Schema(description = "常规时间客队得分", example = "1")
    @Min(value = 0, message = "parameter.error")
    private Integer regularAwayScore;

    /**
     * 加时赛主队得分。
     */
    @Schema(description = "加时赛主队得分", example = "0")
    private Integer extraHomeScore;

    /**
     * 加时赛客队得分。
     */
    @Schema(description = "加时赛客队得分", example = "0")
    private Integer extraAwayScore;

    /**
     * 点球大战主队得分。
     */
    @Schema(description = "点球大战主队得分", example = "0")
    private Integer penaltyHomeScore;

    /**
     * 点球大战客队得分。
     */
    @Schema(description = "点球大战客队得分", example = "0")
    private Integer penaltyAwayScore;

    /**
     * 比赛结束时间。
     */
    @Schema(description = "比赛结束时间", example = "2026-06-15T22:00:00")
    private LocalDateTime matchEndedAt;

    /**
     * 主队点球次数。
     */
    @Schema(description = "主队点球次数", example = "0")
    private Integer homePenaltyCount;

    /**
     * 客队点球次数。
     */
    @Schema(description = "客队点球次数", example = "0")
    private Integer awayPenaltyCount;

    /**
     * 主队角球次数。
     */
    @Schema(description = "主队角球次数", example = "6")
    private Integer homeCornerCount;

    /**
     * 客队角球次数。
     */
    @Schema(description = "客队角球次数", example = "4")
    private Integer awayCornerCount;

    /**
     * 主队界外球次数。
     */
    @Schema(description = "主队界外球次数", example = "18")
    private Integer homeThrowInCount;

    /**
     * 客队界外球次数。
     */
    @Schema(description = "客队界外球次数", example = "15")
    private Integer awayThrowInCount;

    /**
     * 主队犯规次数。
     */
    @Schema(description = "主队犯规次数", example = "10")
    private Integer homeFoulCount;

    /**
     * 客队犯规次数。
     */
    @Schema(description = "客队犯规次数", example = "12")
    private Integer awayFoulCount;

    /**
     * 主队任意球次数。
     */
    @Schema(description = "主队任意球次数", example = "12")
    private Integer homeFreeKickCount;

    /**
     * 客队任意球次数。
     */
    @Schema(description = "客队任意球次数", example = "10")
    private Integer awayFreeKickCount;

    /**
     * 主队红牌数量。
     */
    @Schema(description = "主队红牌数量", example = "0")
    private Integer homeRedCardCount;

    /**
     * 客队红牌数量。
     */
    @Schema(description = "客队红牌数量", example = "0")
    private Integer awayRedCardCount;

    /**
     * 主队黄牌数量。
     */
    @Schema(description = "主队黄牌数量", example = "2")
    private Integer homeYellowCardCount;

    /**
     * 客队黄牌数量。
     */
    @Schema(description = "客队黄牌数量", example = "3")
    private Integer awayYellowCardCount;
}
