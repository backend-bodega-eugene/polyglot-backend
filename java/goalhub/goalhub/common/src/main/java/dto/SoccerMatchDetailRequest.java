package dto;

import lombok.Data;

/**
 * 足球比赛详情查询请求。
 */
@Data
public class SoccerMatchDetailRequest {

    /**
     * 语言编码，默认 en-US。
     */
    private String langCode = "en-US";
}
