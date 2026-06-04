package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "前端赛事赛果响应")
public class AppMatchResultResponse {

    @Schema(description = "赛事ID")
    private Long matchId;

    @Schema(description = "赛事编码")
    private String matchCode;

    @Schema(description = "赛事名称")
    private String matchName;

    @Schema(description = "联盟ID")
    private Long leagueId;

    @Schema(description = "联盟名称")
    private String leagueName;

    @Schema(description = "主队ID")
    private Long homeTeamId;

    @Schema(description = "主队名称")
    private String homeTeamName;

    @Schema(description = "客队ID")
    private Long awayTeamId;

    @Schema(description = "客队名称")
    private String awayTeamName;

    @Schema(description = "赛事状态")
    private String matchStatus;

    @Schema(description = "赛事开始时间")
    private LocalDateTime matchStartTime;

    @Schema(description = "比赛结束时间")
    private LocalDateTime matchEndedAt;

    @Schema(description = "常规时间主队得分")
    private Integer regularHomeScore;

    @Schema(description = "常规时间客队得分")
    private Integer regularAwayScore;

    @Schema(description = "加时赛主队得分")
    private Integer extraHomeScore;

    @Schema(description = "加时赛客队得分")
    private Integer extraAwayScore;

    @Schema(description = "点球大战主队得分")
    private Integer penaltyHomeScore;

    @Schema(description = "点球大战客队得分")
    private Integer penaltyAwayScore;

    @Schema(description = "主队角球次数")
    private Integer homeCornerCount;

    @Schema(description = "客队角球次数")
    private Integer awayCornerCount;

    @Schema(description = "主队红牌数量")
    private Integer homeRedCardCount;

    @Schema(description = "客队红牌数量")
    private Integer awayRedCardCount;

    @Schema(description = "主队黄牌数量")
    private Integer homeYellowCardCount;

    @Schema(description = "客队黄牌数量")
    private Integer awayYellowCardCount;
}