package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "新增联盟球队玩法赔率请求")
public class AddLeagueTeamMarketOddsRequest {

    /**
     * 联赛或杯赛 ID。
     */
    @Schema(description = "联赛/杯赛ID", example = "1")
    private Long leagueId;

    /**
     * 玩法 ID。
     */
    @Schema(description = "玩法ID", example = "1001")
    private Long playId;

    /**
     * 玩法编码。
     */
    @Schema(description = "玩法编码", example = "WIN_DRAW_LOSE")
    private String playCode;

    /**
     * 玩法名称。
     */
    @Schema(description = "玩法名称", example = "胜平负")
    private String playName;

    /**
     * 球队 ID。
     */
    @Schema(description = "球队ID", example = "2001")
    private Long teamId;

    /**
     * 玩法赔率。
     */
    @Schema(description = "玩法赔率", example = "1.95")
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

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;
}
