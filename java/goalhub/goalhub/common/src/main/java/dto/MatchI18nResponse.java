package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 比赛国际化配置响应。
 */
@Schema(description = "比赛国际化配置响应")
@Data
public class MatchI18nResponse {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID")
    private Long id;

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID")
    private Long matchId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码")
    private String langCode;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称")
    private String matchName;

    /**
     * 阶段名称。
     */
    @Schema(description = "阶段名称")
    private String stageName;

    /**
     * 比赛城市。
     */
    @Schema(description = "比赛城市")
    private String city;

    /**
     * 比赛场馆。
     */
    @Schema(description = "比赛场馆")
    private String venue;

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
