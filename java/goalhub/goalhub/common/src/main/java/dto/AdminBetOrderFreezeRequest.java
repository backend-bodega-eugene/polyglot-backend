package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台投注订单冻结请求。
 */
@Data
@Schema(description = "后台投注订单冻结请求")
public class AdminBetOrderFreezeRequest {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单ID")
    private Long orderId;

    /**
     * 冻结备注。
     */
    @Schema(description = "冻结备注")
    private String remark;

    /**
     * 冻结管理员 ID。
     */
    @Schema(description = "冻结管理员 ID")
    private Long adminId;

    /**
     * 冻结管理员用户名。
     */
    @Schema(description = "冻结管理员用户名")
    private String adminUsername;
}
