package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台充值订单审核参数。
 */
@Schema(description = "后台充值订单审核参数")
@Data
public class AdminDepositOrderAuditRequest {

    /**
     * 充值订单 ID。
     */
    @Schema(description = "充值订单 ID", example = "10001")
    private Long id;

    /**
     * 审核状态。
     */
    @Schema(description = "审核状态：APPROVED / REJECTED", example = "APPROVED")
    private String auditStatus;

    /**
     * 审核备注。
     */
    @Schema(description = "审核备注")
    private String auditRemark;

    /**
     * 审核管理员 ID。
     */
    @Schema(description = "审核管理员 ID", example = "1")
    private Long adminId;

    /**
     * 审核管理员用户名。
     */
    @Schema(description = "审核管理员用户名")
    private String adminUsername;
}
