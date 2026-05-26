package dto;

import lombok.Data;

@Data
public class SoccerMatchPageRequest {

    /**
     * 页码，从 1 开始
     */
    private Integer pageIndex;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 联盟ID
     */
    private Long leagueId;

    /**
     * 语言编码，默认 en-US
     */
    private String langCode = "en-US";

    /**
     * 开始时间 UTC
     */
    private String startTimeUtc;

    /**
     * 结束时间 UTC
     */
    private String endTimeUtc;

    /**
     * 球队关键字
     */
    private String teamKeyword;

    /**
     * 比赛状态
     */
    private String status;

    /**
     * 综合关键字
     */
    private String keyword;
}