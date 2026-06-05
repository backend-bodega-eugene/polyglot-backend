package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新联赛国际化配置请求。
 *
 * <p>用于后台维护联赛在指定语言下的名称和简称。</p>
 */
@Schema(description = "更新联赛国际化配置请求")
@Data
public class UpdateLeagueI18nRequest {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID", example = "1")
    private Long id;

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "10")
    private Long leagueId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;

    /**
     * 联赛名称。
     */
    @Schema(description = "联赛名称", example = "英格兰超级联赛")
    private String name;

    /**
     * 联赛简称。
     */
    @Schema(description = "联赛简称", example = "英超")
    private String shortName;
}
