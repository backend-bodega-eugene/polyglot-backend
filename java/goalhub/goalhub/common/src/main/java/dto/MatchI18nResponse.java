package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 比赛国际化配置响应。
 *
 * <p>返回比赛在指定语言下的名称、阶段、城市和场馆信息。</p>
 */
@Schema(description = "比赛国际化配置响应")
@Data
public class MatchI18nResponse {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID", example = "1")
    private Long id;

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1001")
    private Long matchId;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称", example = "曼城 vs 切尔西")
    private String matchName;

    /**
     * 阶段名称。
     */
    @Schema(description = "阶段名称", example = "决赛")
    private String stageName;

    /**
     * 比赛城市。
     */
    @Schema(description = "比赛城市", example = "伦敦")
    private String city;

    /**
     * 比赛场馆。
     */
    @Schema(description = "比赛场馆", example = "温布利球场")
    private String venue;

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
