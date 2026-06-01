package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新赛事玩法赔率请求。
 */
@Data
@Schema(description = "更新赛事玩法赔率请求")
public class UpdateMatchMarketOptionRequest {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID")
    private Long id;

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
