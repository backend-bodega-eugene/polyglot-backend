package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增赛事玩法赔率请求。
 *
 * <p>用于后台为指定比赛新增某个玩法选项的赔率配置。</p>
 */
@Data
@Schema(description = "新增赛事玩法赔率请求")
public class AddMatchMarketOptionRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1")
    @NotNull(message = "parameter.error")
    private Long matchId;

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID", example = "1")
    @NotNull(message = "parameter.error")
    private Long marketId;

    /**
     * 投注玩法选项 ID。
     */
    @Schema(description = "投注玩法选项 ID", example = "1")
    @NotNull(message = "parameter.error")
    private Long marketOptionId;

    /**
     * 赔率。
     */
    @Schema(description = "赔率", example = "1.95")
    @NotNull(message = "bet.odds.invalid")
    @DecimalMin(value = "0.01", message = "bet.odds.invalid")
    private BigDecimal odds;

    /**
     * 是否前端可见。
     */
    @Schema(description = "是否前端可见，1 可见，0 不可见", example = "1")
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer visible;

    /**
     * 投注状态。
     */
    @Schema(description = "投注状态", example = "OPEN")
    private String betStatus;

    /**
     * 排序值。
     */
    @Schema(description = "排序值")
    private Integer sortOrder;
}
