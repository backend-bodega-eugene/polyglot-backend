package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台新增球队国际化配置请求。
 *
 * <p>用于后台新增球队在指定语言下的展示名称。</p>
 */
@Schema(description = "后台新增球队国际化配置请求")
@Data
public class AddTeamI18nRequest {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID", example = "1")
    private Long teamId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh_CN")
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
