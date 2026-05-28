package dto;

import lombok.Data;

/**
 * 足球比赛分页查询请求。
 */
@Data
public class SoccerMatchPageRequest {

    /**
     * 页码，从 1 开始。
     */
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    private Integer pageSize;

    /**
     * 联赛 ID。
     */
    private Long leagueId;

    /**
     * 语言编码，默认 en-US。
     */
    private String langCode = "en-US";

    /**
     * 查询开始时间，UTC 时间字符串。
     */
    private String startTimeUtc;

    /**
     * 查询结束时间，UTC 时间字符串。
     */
    private String endTimeUtc;

    /**
     * 球队关键字。
     */
    private String teamKeyword;

    /**
     * 比赛状态。
     */
    private String status;

    /**
     * 通用关键字。
     */
    private String keyword;
}
