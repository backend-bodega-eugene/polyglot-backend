package dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppDepositOrderResponse {

    private Long id;

    private String orderNo;

    private String currencyCode;

    private BigDecimal amount;

    private BigDecimal actualAmount;

    private String status;

    private String chainType;

    private String txHash;

    private String remark;

    private String auditRemark;

    private LocalDateTime auditTime;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}