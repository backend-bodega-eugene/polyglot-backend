package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * App 充值订单创建请求。
 *
 * <p>用于 App 端提交充值申请和链上交易信息。</p>
 */
@Schema(description = "App 充值订单创建请求")
@Data
public class AppDepositOrderCreateRequest {

    /**
     * 充值金额。
     */
    @Schema(description = "充值金额", example = "100.0000")
    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.0", inclusive = false, message = "充值金额必须大于0")
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
     * 链上交易哈希。
     */
    @Schema(description = "链上交易哈希")
    @Size(max = 128, message = "链上交易哈希长度不能超过128")
    private String txHash;

    /**
     * 用户备注。
     */
    @Schema(description = "用户备注")
    @Size(max = 500, message = "备注长度不能超过500")
    private String remark;
}
