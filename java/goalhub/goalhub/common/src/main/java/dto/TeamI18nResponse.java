package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 球队国际化配置响应。
 *
 * <p>返回球队在指定语言下的名称、简称和维护时间。</p>
 */
@Schema(description = "球队国际化配置响应")
@Data
public class TeamI18nResponse {

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
    @Schema(description = "球队名称", example = "曼城")
    private String name;

    /**
     * 球队简称。
     */
    @Schema(description = "球队简称", example = "MCI")
    private String shortName;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间", example = "2026-06-04T12:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间", example = "2026-06-04T12:30:00")
    private LocalDateTime updatedAt;
}
