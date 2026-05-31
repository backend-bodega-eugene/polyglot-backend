package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台新增联赛国际化配置请求。
 */
@Schema(description = "后台新增联赛国际化配置请求")
@Data
public class AddLeagueI18nRequest {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID")
    private Long leagueId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码")
    private String langCode;

    /**
     * 联赛名称。
     */
    @Schema(description = "联赛名称")
    private String name;

    /**
     * 联赛简称。
     */
    @Schema(description = "联赛简称")
    private String shortName;
}
