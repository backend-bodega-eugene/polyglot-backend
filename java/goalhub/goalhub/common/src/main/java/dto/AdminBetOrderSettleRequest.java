package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台投注订单结算请求。
 */
@Data
@Schema(description = "后台投注订单结算请求")
public class AdminBetOrderSettleRequest {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单ID")
    private Long orderId;

    /**
     * 结算备注。
     */
    @Schema(description = "结算备注")
    private String remark;

    /**
     * 结算管理员 ID。
     */
    @Schema(description = "结算管理员 ID")
    private Long adminId;

    /**
     * 结算管理员用户名。
     */
    @Schema(description = "结算管理员用户名")
    private String adminUsername;
}
