package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联赛国际化配置响应。
 *
 * <p>返回联赛在指定语言下的名称、简称和维护时间。</p>
 */
@Schema(description = "联赛国际化配置响应")
@Data
public class LeagueI18nResponse {

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
