package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 赛事玩法赔率响应。
 */
@Data
@Schema(description = "赛事玩法赔率响应")
public class MatchMarketOptionResponse {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID")
    private Long id;

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID")
    private Long matchId;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码")
    private String matchCode;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称")
    private String matchName;

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID")
    private Long marketId;

    /**
     * 投注玩法选项 ID。
     */
    @Schema(description = "投注玩法选项 ID")
    private Long marketOptionId;

    /**
     * 投注玩法编码。
     */
    @Schema(description = "投注玩法编码")
    private String marketCode;

    /**
     * 投注玩法名称。
     */
    @Schema(description = "投注玩法名称")
    private String marketName;

    /**
     * 投注玩法选项编码。
     */
    @Schema(description = "投注玩法选项编码")
    private String marketOptionCode;

    /**
     * 投注玩法选项名称。
     */
    @Schema(description = "投注玩法选项名称")
    private String marketOptionName;

    /**
     * 赔率。
     */
    @Schema(description = "赔率")
    private BigDecimal odds;

    /**
     * 是否前端可见。
     */
    @Schema(description = "是否前端可见")
    private Integer visible;

    /**
     * 投注状态。
     */
    @Schema(description = "投注状态")
    private String betStatus;

    /**
     * 排序值。
     */
    @Schema(description = "排序值")
    private Integer sortOrder;

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
