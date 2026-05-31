package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 球队国际化配置响应。
 */
@Schema(description = "球队国际化配置响应")
@Data
public class TeamI18nResponse {

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

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
