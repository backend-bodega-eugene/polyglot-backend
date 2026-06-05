package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 更新赛事玩法赔率请求。
 *
 * <p>用于后台调整赛事玩法选项赔率、前端可见状态、投注状态和排序。</p>
 */
@Data
@Schema(description = "更新赛事玩法赔率请求")
public class UpdateMatchMarketOptionRequest {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID", example = "1")
    @NotNull(message = "parameter.error")
    private Long id;

    /**
     * 赔率。
     */
    @Schema(description = "赔率", example = "1.85")
    @DecimalMin(value = "0.01", message = "bet.odds.invalid")
    private BigDecimal odds;

    /**
     * 是否前端可见。
     */
    @Schema(description = "是否前端可见", example = "1")
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
    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
}
