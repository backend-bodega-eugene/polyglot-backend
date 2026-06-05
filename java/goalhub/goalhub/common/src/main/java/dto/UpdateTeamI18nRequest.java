package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新球队国际化配置请求。
 *
 * <p>用于后台维护球队在指定语言下的名称和简称。</p>
 */
@Schema(description = "更新球队国际化配置请求")
@Data
public class UpdateTeamI18nRequest {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID", example = "1")
    private Long id;

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID", example = "1001")
    private Long teamId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;

    /**
     * 球队名称。
     */
    @Schema(description = "球队名称", example = "巴西")
    private String name;

    /**
     * 球队简称。
     */
    @Schema(description = "球队简称", example = "BRA")
    private String shortName;
}
