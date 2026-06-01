package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增赛事玩法赔率请求。
 */
@Data
@Schema(description = "新增赛事玩法赔率请求")
public class AddMatchMarketOptionRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID")
    private Long matchId;

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
}
