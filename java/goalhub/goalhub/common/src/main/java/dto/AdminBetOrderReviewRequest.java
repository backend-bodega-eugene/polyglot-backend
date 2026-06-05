package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台投注订单审核请求。
 *
 * <p>用于后台管理员对投注订单进行人工审核。</p>
 */
@Data
@Schema(description = "后台投注订单审核请求")
public class AdminBetOrderReviewRequest {

    /**
     * 订单 ID。
     */
    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单 ID", example = "10001")
    private Long orderId;

    /**
     * 审核结果。
     */
    @NotBlank(message = "审核结果不能为空")
    @Size(max = 32, message = "审核结果长度不能超过32")
    @Schema(description = "审核结果：PENDING, FROZEN, LOSE, WIN, REFUNDED, CANCELLED", example = "WIN")
    private String reviewResult;

    /**
     * 审核备注。
     */
    @NotBlank(message = "审核备注不能为空")
    @Size(max = 255, message = "审核备注长度不能超过255")
    @Schema(description = "审核备注")
    private String remark;

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
