package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 足球联赛响应。
 */
@Schema(description = "足球联赛响应")
@Data
public class SoccerLeagueResponse {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    private Long id;

    /**
     * 联赛编码，例如 FIFA_WORLD_CUP_2026。
     */
    @Schema(description = "联赛编码", example = "FIFA_WORLD_CUP_2026")
    private String code;

    /**
     * 联赛名称，按 langCode 返回对应语言。
     */
    @Schema(description = "联赛名称，按 langCode 返回对应语言", example = "FIFA World Cup 2026")
    private String name;

    /**
     * 联赛简称，按 langCode 返回对应语言。
     */
    @Schema(description = "联赛简称，按 langCode 返回对应语言", example = "World Cup")
    private String shortName;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家", example = "United States, Canada, Mexico")
    private String hostCountry;

    /**
     * 联赛 Logo 地址。
     */
    @Schema(description = "联赛 Logo 地址", example = "https://example.com/logo.png")
    private String logoUrl;
}
