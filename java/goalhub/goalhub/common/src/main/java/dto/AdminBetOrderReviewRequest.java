package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台投注订单审核请求。
 */
@Data
@Schema(description = "后台投注订单审核请求")
public class AdminBetOrderReviewRequest {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单ID")
    private Long orderId;

    /**
     * 审核结果。
     */
    @Schema(description = "审核结果：WIN,LOSE,REFUNDED,CANCELLED")
    private String reviewResult;

    /**
     * 审核备注。
     */
    @Schema(description = "审核备注")
    private String remark;

    /**
     * 审核管理员 ID。
     */
    @Schema(description = "审核管理员 ID")
    private Long adminId;

    /**
     * 审核管理员用户名。
     */
    @Schema(description = "审核管理员用户名")
    private String adminUsername;
}
