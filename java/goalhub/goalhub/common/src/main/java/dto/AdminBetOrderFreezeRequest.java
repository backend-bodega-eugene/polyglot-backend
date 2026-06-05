package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台投注订单冻结请求。
 *
 * <p>用于后台管理员冻结指定投注订单。</p>
 */
@Data
@Schema(description = "后台投注订单冻结请求")
public class AdminBetOrderFreezeRequest {

    /**
     * 订单 ID。
     */
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单 ID", example = "10001")
    private Long orderId;

    /**
     * 冻结备注。
     */
    @NotBlank(message = "冻结备注不能为空")
    @Size(max = 255, message = "冻结备注长度不能超过255")
    @Schema(description = "冻结备注")
    private String remark;

    /**
     * 冻结管理员 ID。
     */
    @Schema(description = "冻结管理员 ID", example = "1")
    private Long adminId;

    /**
     * 冻结管理员用户名。
     */
    @Schema(description = "冻结管理员用户名")
    private String adminUsername;
}
