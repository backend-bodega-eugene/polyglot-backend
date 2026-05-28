package dto;

import lombok.Data;

/**
 * 足球比赛详情响应。
 */
@Data
public class SoccerMatchDetailResponse {

    /**
     * 比赛 ID。
     */
    private Long id;

    /**
     * 联赛 ID。
     */
    private Long leagueId;

    /**
     * 联赛名称。
     */
    private String leagueName;

    /**
     * 比赛编码。
     */
    private String matchCode;

    /**
     * 比赛名称。
     */
    private String matchName;

    /**
     * 阶段编码。
     */
    private String stageCode;

    /**
     * 阶段名称。
     */
    private String stageName;

    /**
     * 主队 ID。
     */
    private Long homeTeamId;

    /**
     * 主队名称。
     */
    private String homeTeamName;

    /**
     * 主队简称。
     */
    private String homeTeamShortName;

    /**
     * 客队 ID。
     */
    private Long awayTeamId;

    /**
     * 客队名称。
     */
    private String awayTeamName;

    /**
     * 客队简称。
     */
    private String awayTeamShortName;

    /**
     * 计划开赛时间，UTC 时间字符串。
     */
    private String scheduledStartTimeUtc;

    /**
     * 实际开赛时间，UTC 时间字符串。
     */
    private String actualStartTimeUtc;

    /**
     * 实际结束时间，UTC 时间字符串。
     */
    private String actualEndTimeUtc;

    /**
     * 主办国家。
     */
    private String hostCountry;

    /**
     * 比赛城市。
     */
    private String city;

    /**
     * 比赛场馆。
     */
    private String venue;

    /**
     * 比赛状态。
     */
    private String status;
}
