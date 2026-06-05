package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 前端下注请求。
 *
 * <p>提交赛事玩法赔率 ID 和下注金额，用于创建投注订单。</p>
 */
@Data
@Schema(description = "前端下注请求")
public class PlaceBetOrderRequest {

    /**
     * 赛事玩法赔率 ID。
     */
    @NotNull(message = "赛事玩法赔率ID不能为空")
    @Schema(description = "赛事玩法赔率ID", example = "1")
    private Long matchMarketOptionId;

    /**
     * 下注金额。
     */
    @NotNull(message = "下注金额不能为空")
    @DecimalMin(value = "0.01", message = "下注金额必须大于0")
    @Digits(integer = 18, fraction = 2, message = "下注金额最多保留2位小数")
    @Schema(description = "下注金额", example = "50.00")
    private BigDecimal amount;
}
