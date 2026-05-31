package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新球队国际化配置请求。
 */
@Schema(description = "更新球队国际化配置请求")
@Data
public class UpdateTeamI18nRequest {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID")
    private Long id;

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID")
    private Long teamId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码")
    private String langCode;

    /**
     * 球队名称。
     */
    @Schema(description = "球队名称")
    private String name;

    /**
     * 球队简称。
     */
    @Schema(description = "球队简称")
    private String shortName;
}
