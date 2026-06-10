package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 默认账户余额变更请求。
 *
 * <p>用于对用户默认账户执行余额增加或扣减操作。</p>
 */
@Schema(description = "默认账户余额变更请求")
@Data
public class DefaultAccountBalanceChangeRequest {

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 变更金额。
     */
    @Schema(description = "变更金额", example = "100.0000")
    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "金额必须大于0")
    private BigDecimal amount;

    /**
     * 业务 ID。
     */
    @Schema(description = "业务 ID", example = "ORDER202606040001")
    @NotBlank(message = "业务ID不能为空")
    private String bizId;

    /**
     * 备注说明。
     */
    @Schema(description = "备注说明")
    private String remark;
}
