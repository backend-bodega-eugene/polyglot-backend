package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台投注订单结算请求。
 *
 * <p>用于后台管理员结算指定投注订单。</p>
 */
@Data
@Schema(description = "后台投注订单结算请求")
public class AdminBetOrderSettleRequest {

    /**
     * 订单 ID。
     */
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单 ID", example = "10001")
    private Long orderId;

    /**
     * 结算备注。
     */
    @NotBlank(message = "结算备注不能为空")
    @Size(max = 255, message = "结算备注长度不能超过255")
    @Schema(description = "结算备注")
    private String remark;

    /**
     * 结算管理员 ID。
     */
    @Schema(description = "结算管理员 ID", example = "1")
    private Long adminId;

    /**
     * 结算管理员用户名。
     */
    @Schema(description = "结算管理员用户名")
    private String adminUsername;
}
