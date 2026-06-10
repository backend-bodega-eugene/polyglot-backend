package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * App 提现订单创建请求。
 *
 * <p>用于 App 端提交提现申请和提现地址信息。</p>
 */
@Schema(description = "App 提现订单创建请求")
@Data
public class AppWithdrawOrderCreateRequest {

    /**
     * 提现金额。
     */
    @Schema(description = "提现金额", example = "100.0000")
    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "提现金额必须大于0")
    private BigDecimal amount;

    /**
     * 币种编码。
     */
    @Schema(description = "币种编码", example = "USDT")
    @Size(max = 16, message = "币种编码长度不能超过16")
    private String currencyCode;

    /**
     * 链类型。
     */
    @Schema(description = "链类型", example = "TRC20")
    @Size(max = 32, message = "链类型长度不能超过32")
    private String chainType;

    /**
     * 提现地址。
     */
    @Schema(description = "提现地址")
    @NotBlank(message = "提现地址不能为空")
    @Size(max = 255, message = "提现地址长度不能超过255")
    private String withdrawAddress;

    /**
     * 用户备注。
     */
    @Schema(description = "用户备注")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
    /**
     * 资金密码。
     */
    @Schema(description = "资金密码")
    @NotBlank(message = "资金密码不能为空")
    @Size(max = 100, message = "资金密码长度不能超过100")
    private String fundPassword;
}
