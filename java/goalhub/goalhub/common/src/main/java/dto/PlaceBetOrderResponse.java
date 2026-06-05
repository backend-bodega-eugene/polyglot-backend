package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 前端下单响应。
 *
 * <p>返回投注订单编号、订单状态、赔率、预估收益和下注后的账户余额。</p>
 */
@Data
@Schema(description = "前端下单响应")
public class PlaceBetOrderResponse {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单ID", example = "10001")
    private Long orderId;

    /**
     * 订单号。
     */
    @Schema(description = "订单号", example = "BET202606040001")
    private String orderNo;

    /**
     * 订单状态。
     */
    @Schema(description = "订单状态", example = "PENDING")
    private String status;

    /**
     * 下注金额。
     */
    @Schema(description = "下注金额", example = "50.00")
    private BigDecimal betAmount;

    /**
     * 赔率。
     */
    @Schema(description = "赔率", example = "1.85")
    private BigDecimal odds;

    /**
     * 预计盈利。
     */
    @Schema(description = "预计盈利", example = "42.50")
    private BigDecimal expectedProfit;

    /**
     * 预计返还。
     */
    @Schema(description = "预计返还", example = "92.50")
    private BigDecimal expectedReturn;

    /**
     * 下注后余额。
     */
    @Schema(description = "下注后余额", example = "950.00")
    private BigDecimal balanceAfter;
}
