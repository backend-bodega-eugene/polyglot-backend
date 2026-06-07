package dto;

import lombok.Data;

/**
 * 后台充值订单分页查询参数。
 */
@Data
public class AdminDepositOrderPageRequest {

    private Long userId;

    private String orderNo;

    private String currencyCode;

    /**
     * PENDING / APPROVED / REJECTED
     */
    private String status;

    private String chainType;

    private String txHash;

    private Integer pageIndex;

    private Integer pageSize;
}