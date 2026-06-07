package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台充值订单响应。
 */
@Schema(description = "后台充值订单响应")
@Data
public class AdminDepositOrderResponse {

    /**
     * 充值订单 ID。
     */
    @Schema(description = "充值订单 ID", example = "10001")
    private Long id;

    /**
     * 充值订单号。
     */
    @Schema(description = "充值订单号")
    private String orderNo;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    /**
     * 币种编码。
     */
    @Schema(description = "币种编码", example = "USDT")
    private String currencyCode;

    /**
     * 充值申请金额。
     */
    @Schema(description = "充值申请金额")
    private BigDecimal amount;

    /**
     * 实际到账金额。
     */
    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    /**
     * 订单状态。
     */
    @Schema(description = "订单状态", example = "PENDING")
    private String status;

    /**
     * 链类型。
     */
    @Schema(description = "链类型", example = "TRC20")
    private String chainType;

    /**
     * 链上交易哈希。
     */
    @Schema(description = "链上交易哈希")
    private String txHash;

    /**
     * 用户备注。
     */
    @Schema(description = "用户备注")
    private String remark;

    /**
     * 审核备注。
     */
    @Schema(description = "审核备注")
    private String auditRemark;

    /**
     * 审核管理员 ID。
     */
    @Schema(description = "审核管理员 ID", example = "1")
    private Long auditAdminId;

    /**
     * 审核管理员名称。
     */
    @Schema(description = "审核管理员名称")
    private String auditAdminName;

    /**
     * 审核时间。
     */
    @Schema(description = "审核时间")
    private LocalDateTime auditTime;

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
