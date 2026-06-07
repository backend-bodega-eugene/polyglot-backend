package dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台提现订单响应。
 */
@Data
public class AdminWithdrawOrderResponse {

    private Long id;

    private String orderNo;

    private Long userId;

    private String currencyCode;

    private BigDecimal amount;

    private BigDecimal actualAmount;

    private BigDecimal feeAmount;

    private String status;

    private String chainType;

    private String withdrawAddress;

    private String txHash;

    private String remark;

    private String auditRemark;

    private Long auditAdminId;
    private String auditAdminName;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}