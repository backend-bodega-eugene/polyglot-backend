package dto;

import lombok.Data;

/**
 * 热门足球比赛查询请求。
 */
@Data
public class SoccerHotMatchRequest {

    /**
     * 语言编码，默认 en-US。
     */
    private String langCode = "en-US";

    /**
     * 返回数量，默认 10。
     */
    private Integer limit = 10;
}
