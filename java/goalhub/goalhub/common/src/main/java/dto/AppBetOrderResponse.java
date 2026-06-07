package dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AppBetOrderResponse {

    private Long orderId;

    private String orderNo;

    private BigDecimal totalBetAmount;

    private BigDecimal totalExpectedProfit;

    private BigDecimal totalExpectedReturn;

    private String currencyCode;

    private String status;

    private String systemResult;

    private String reviewResult;

    private BigDecimal settleAmount;

    private String settleRemark;

    private LocalDateTime settledAt;

    private LocalDateTime createdAt;

    private List<AppBetOrderItemResponse> items;
}