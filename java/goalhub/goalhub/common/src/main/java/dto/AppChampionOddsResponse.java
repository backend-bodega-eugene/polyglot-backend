package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "前端冠军赔率响应")
public class AppChampionOddsResponse {

    /**
     * 冠军赔率 ID。
     */
    @Schema(description = "冠军赔率ID", example = "1")
    private Long championOddsId;

    /**
     * 联赛或杯赛 ID。
     */
    @Schema(description = "联赛/杯赛ID", example = "1")
    private Long leagueId;

    /**
     * 联赛或杯赛名称。
     */
    @Schema(description = "联赛/杯赛名称", example = "世界杯")
    private String leagueName;

    /**
     * 联赛或杯赛 Logo 地址。
     */
    @Schema(description = "联赛/杯赛Logo地址")
    private String leagueLogoUrl;

    /**
     * 球队 ID。
     */
    @Schema(description = "球队ID", example = "1001")
    private Long teamId;

    /**
     * 球队编码。
     */
    @Schema(description = "球队编码", example = "FRA")
    private String teamCode;

    /**
     * 球队名称。
     */
    @Schema(description = "球队名称", example = "France")
    private String teamName;

    /**
     * 球队 Logo 地址。
     */
    @Schema(description = "球队Logo地址")
    private String teamLogoUrl;

    /**
     * 冠军赔率。
     */
    @Schema(description = "冠军赔率", example = "2.50")
    private BigDecimal odds;

    /**
     * 是否在前台展示。
     */
    @Schema(description = "是否可见，1可见，0隐藏", example = "1")
    private Integer visible;

    /**
     * 下注状态。
     */
    @Schema(description = "下注状态", example = "OPEN")
    private String betStatus;

    /**
     * 排序值。
     */
    @Schema(description = "排序值，数值越小越靠前", example = "10")
    private Integer sortOrder;
}
