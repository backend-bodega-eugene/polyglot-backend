package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户账户响应。
 */
@Data
@Schema(description = "用户账户响应")
public class UserAccountResponse {

    /**
     * 账户 ID。
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 货币类型。
     */
    @Schema(description = "货币类型")
    private String currencyCode;

    /**
     * 账户余额。
     */
    @Schema(description = "账户余额")
    private BigDecimal balance;

    /**
     * 冻结余额。
     */
    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    /**
     * 可用余额。
     */
    @Schema(description = "可用余额")
    private BigDecimal availableBalance;

    /**
     * 账户状态。
     */
    @Schema(description = "账户状态")
    private Integer status;
}
