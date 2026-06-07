package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * App 投注订单响应。
 *
 * <p>返回 App 端投注订单主表信息及订单明细列表。</p>
 */
@Schema(description = "App 投注订单响应")
@Data
public class AppBetOrderResponse {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单 ID", example = "10001")
    private Long orderId;

    /**
     * 订单号。
     */
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 总下注金额。
     */
    @Schema(description = "总下注金额")
    private BigDecimal totalBetAmount;

    /**
     * 总预计盈利金额。
     */
    @Schema(description = "总预计盈利金额")
    private BigDecimal totalExpectedProfit;

    /**
     * 总预计返还金额。
     */
    @Schema(description = "总预计返还金额")
    private BigDecimal totalExpectedReturn;

    /**
     * 币种编码。
     */
    @Schema(description = "币种编码", example = "USDT")
    private String currencyCode;

    /**
     * 订单状态。
     */
    @Schema(description = "订单状态", example = "PENDING")
    private String status;

    /**
     * 系统判定结果。
     */
    @Schema(description = "系统判定结果")
    private String systemResult;

    /**
     * 审核结果。
     */
    @Schema(description = "审核结果")
    private String reviewResult;

    /**
     * 实际结算金额。
     */
    @Schema(description = "实际结算金额")
    private BigDecimal settleAmount;

    /**
     * 结算备注。
     */
    @Schema(description = "结算备注")
    private String settleRemark;

    /**
     * 结算时间。
     */
    @Schema(description = "结算时间")
    private LocalDateTime settledAt;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 订单明细列表。
     */
    @Schema(description = "订单明细列表")
    private List<AppBetOrderItemResponse> items;
}
