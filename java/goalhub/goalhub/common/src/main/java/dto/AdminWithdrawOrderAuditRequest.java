package dto;

import lombok.Data;

/**
 * 后台提现订单审核参数。
 */
@Data
public class AdminWithdrawOrderAuditRequest {

    private Long id;

    /**
     * APPROVED / REJECTED
     */
    private String auditStatus;

    private String auditRemark;

    private Long adminId;

    private String adminUsername;
}