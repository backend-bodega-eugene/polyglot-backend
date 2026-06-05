package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单扣减默认 USDT 账户请求。
 *
 * <p>用于订单创建或结算流程中，从用户默认 USDT 账户扣减指定金额。</p>
 */
@Data
@Schema(description = "订单扣减默认USDT账户请求")
public class DeductDefaultAccountRequest {

    /**
     * 用户 ID。
     */
    @NotNull(message = "用户ID不能为空")
    @Schema(description = "用户ID", example = "10001")
    private Long userId;

    /**
     * 扣减金额。
     */
    @NotNull(message = "扣减金额不能为空")
    @DecimalMin(value = "0.01", message = "扣减金额必须大于0")
    @Digits(integer = 18, fraction = 2, message = "扣减金额最多保留2位小数")
    @Schema(description = "扣减金额", example = "50.00")
    private BigDecimal amount;

    /**
     * 业务 ID，一般为订单号。
     */
    @NotBlank(message = "业务ID不能为空")
    @Size(max = 64, message = "业务ID长度不能超过64")
    @Schema(description = "业务ID，一般为订单号", example = "ORDER202606040001")
    private String bizId;

    /**
     * 备注说明。
     */
    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注", example = "投注订单扣款")
    private String remark;
}
