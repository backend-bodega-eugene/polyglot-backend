package dto;

import lombok.Data;

/**
 * 后台充值订单审核参数。
 */
@Data
public class AdminDepositOrderAuditRequest {

    private Long id;

    /**
     * APPROVED / REJECTED
     */
    private String auditStatus;

    private String auditRemark;

    private Long adminId;

    private String adminUsername;
}