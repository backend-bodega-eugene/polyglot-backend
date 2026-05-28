package dto;

import lombok.Data;

/**
 * 足球联赛响应。
 */
@Data
public class SoccerLeagueResponse {

    /**
     * 联赛 ID。
     */
    private Long id;

    /**
     * 联赛编码，例如 FIFA_WORLD_CUP_2026。
     */
    private String code;

    /**
     * 联赛名称，按 langCode 返回对应语言。
     */
    private String name;

    /**
     * 联赛简称，按 langCode 返回对应语言。
     */
    private String shortName;

    /**
     * 主办国家。
     */
    private String hostCountry;

    /**
     * 联赛 Logo 地址。
     */
    private String logoUrl;
}
