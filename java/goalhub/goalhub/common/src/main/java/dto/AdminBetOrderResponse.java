package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台投注订单响应。
 *
 * <p>返回后台投注订单主表信息，包含下注金额、余额快照、审核与结算信息。</p>
 */
@Data
@Schema(description = "后台投注订单响应")
public class AdminBetOrderResponse {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单 ID", example = "10001")
    private Long orderId;

    /**
     * 订单号。
     */
    @Schema(description = "订单号", example = "BO202606040001")
    private String orderNo;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    /**
     * 账户 ID。
     */
    @Schema(description = "账户 ID", example = "20001")
    private Long accountId;

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
     * 下注前余额。
     */
    @Schema(description = "下注前余额")
    private BigDecimal balanceBefore;

    /**
     * 下注后余额。
     */
    @Schema(description = "下注后余额")
    private BigDecimal balanceAfter;

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
     * 审核管理员 ID。
     */
    @Schema(description = "审核管理员ID")
    private Long reviewAdminId;

    /**
     * 审核管理员名称。
     */
    @Schema(description = "审核管理员名称")
    private String reviewAdminName;

    /**
     * 审核备注。
     */
    @Schema(description = "审核备注")
    private String reviewRemark;

    /**
     * 审核时间。
     */
    @Schema(description = "审核时间")
    private LocalDateTime reviewedAt;

    /**
     * 实际结算金额。
     */
    @Schema(description = "实际结算金额")
    private BigDecimal settleAmount;

    /**
     * 结算管理员 ID。
     */
    @Schema(description = "结算管理员ID")
    private Long settleAdminId;

    /**
     * 结算管理员名称。
     */
    @Schema(description = "结算管理员名称")
    private String settleAdminName;

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
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 投注类型。
     */
    @Schema(description = "投注类型", example = "MATCH")
    private String betType;

    /**
     * 联赛或杯赛 ID。
     */
    @Schema(description = "联赛/杯赛ID", example = "1")
    private Long leagueId;

    /**
     * 联赛或杯赛名称。
     */
    @Schema(description = "联赛/杯赛名称", example = "世界杯")
    private String leagueName;

    /**
     * 冠军投注球队 ID。
     */
    @Schema(description = "冠军投注球队ID", example = "1001")
    private Long championTeamId;

    /**
     * 冠军投注球队名称。
     */
    @Schema(description = "冠军投注球队名称", example = "France")
    private String championTeamName;
}
