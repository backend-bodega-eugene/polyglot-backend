package dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台充值订单响应。
 */
@Data
public class AdminDepositOrderResponse {

    private Long id;

    private String orderNo;

    private Long userId;

    private String currencyCode;

    private BigDecimal amount;

    private BigDecimal actualAmount;

    private String status;

    private String chainType;

    private String txHash;

    private String remark;

    private String auditRemark;

    private Long auditAdminId;
    private String auditAdminName;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}