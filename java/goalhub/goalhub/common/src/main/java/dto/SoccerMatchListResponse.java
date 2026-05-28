package dto;

import lombok.Data;

/**
 * 足球比赛列表响应。
 */
@Data
public class SoccerMatchListResponse {

    /**
     * 比赛 ID。
     */
    private Long id;

    /**
     * 比赛名称。
     */
    private String matchName;

    /**
     * 主队名称。
     */
    private String homeTeamName;

    /**
     * 客队名称。
     */
    private String awayTeamName;

    /**
     * 计划开赛时间，UTC 时间字符串。
     */
    private String scheduledStartTimeUtc;

    /**
     * 比赛状态。
     */
    private String status;
}
