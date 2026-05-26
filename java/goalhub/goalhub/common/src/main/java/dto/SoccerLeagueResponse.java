package dto;

import lombok.Data;

@Data
public class SoccerLeagueResponse {

    private Long id;

    /**
     * 联盟编码，例如 FIFA_WORLD_CUP_2026
     */
    private String code;

    /**
     * 联盟名称，根据 langCode 返回
     */
    private String name;

    /**
     * 联盟简称，根据 langCode 返回
     */
    private String shortName;

    /**
     * 举办国家
     */
    private String hostCountry;

    /**
     * 联盟Logo地址
     */
    private String logoUrl;
}