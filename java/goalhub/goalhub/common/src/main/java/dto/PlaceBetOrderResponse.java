package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "前端下单响应")
public class PlaceBetOrderResponse {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "订单状态")
    private String status;

    @Schema(description = "下注金额")
    private BigDecimal betAmount;

    @Schema(description = "赔率")
    private BigDecimal odds;

    @Schema(description = "预计盈利")
    private BigDecimal expectedProfit;

    @Schema(description = "预计返还")
    private BigDecimal expectedReturn;

    @Schema(description = "下注后余额")
    private BigDecimal balanceAfter;
}