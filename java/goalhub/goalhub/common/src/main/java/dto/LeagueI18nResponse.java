package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 联赛国际化配置响应。
 */
@Schema(description = "联赛国际化配置响应")
@Data
public class LeagueI18nResponse {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID")
    private Long id;

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
