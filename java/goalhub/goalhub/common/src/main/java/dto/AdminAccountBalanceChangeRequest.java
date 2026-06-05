package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台账户余额变更请求。
 *
 * <p>用于后台对用户账户执行加余额或扣余额操作。</p>
 */
@Data
@Schema(description = "后台账户余额变更请求")
public class AdminAccountBalanceChangeRequest {

    /**
     * 账户 ID。
     */
    @NotNull(message = "账户ID不能为空")
    @Schema(description = "账户 ID", example = "20001")
    private Long accountId;

    /**
     * 变更金额。
     */
    @NotNull(message = "变更金额不能为空")
    @DecimalMin(value = "0.01", message = "变更金额必须大于0")
    @Digits(integer = 18, fraction = 2, message = "变更金额最多保留2位小数")
    @Schema(description = "变更金额", example = "100.00")
    private BigDecimal amount;

    /**
     * 业务 ID。
     */
    @Size(max = 64, message = "业务ID长度不能超过64")
    @Schema(description = "业务ID", example = "BO202606040001")
    private String bizId;

    /**
     * 备注。
     */
    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
