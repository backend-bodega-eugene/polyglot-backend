package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新比赛国际化配置请求。
 *
 * <p>用于后台维护比赛在指定语言下的名称、阶段、城市和场馆信息。</p>
 */
@Schema(description = "更新比赛国际化配置请求")
@Data
public class UpdateMatchI18nRequest {

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
    @Schema(description = "比赛名称", example = "巴西 vs 阿根廷")
    private String matchName;

    /**
     * 阶段名称。
     */
    @Schema(description = "阶段名称", example = "决赛")
    private String stageName;

    /**
     * 比赛城市。
     */
    @Schema(description = "比赛城市", example = "纽约")
    private String city;

    /**
     * 比赛场馆。
     */
    @Schema(description = "比赛场馆", example = "MetLife Stadium")
    private String venue;
}
