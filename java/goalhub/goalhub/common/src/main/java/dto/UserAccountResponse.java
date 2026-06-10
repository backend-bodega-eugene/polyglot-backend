package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户账户响应。
 *
 * <p>返回用户账户的币种、余额、冻结余额、可用余额和账户状态。</p>
 */
@Data
@Schema(description = "用户账户响应")
public class UserAccountResponse {

    /**
     * 账户 ID。
     */
    @Schema(description = "账户ID", example = "20001")
    private Long accountId;

    /**
     * 货币类型。
     */
    @Schema(description = "货币类型", example = "USDT")
    private String currencyCode;

    /**
     * 账户余额。
     */
    @Schema(description = "账户余额", example = "1000.0000")
    private BigDecimal balance;

    /**
     * 冻结余额。
     */
    @Schema(description = "冻结余额", example = "100.0000")
    private BigDecimal frozenBalance;

    /**
     * 可用余额。
     */
    @Schema(description = "可用余额", example = "900.0000")
    private BigDecimal availableBalance;

    /**
     * 账户状态。
     */
    @Schema(description = "账户状态", example = "1")
    private Integer status;
}
