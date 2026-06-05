package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 前端用户默认余额响应。
 *
 * <p>返回当前用户默认账户的余额、冻结余额和可用余额。</p>
 */
@Data
@Schema(description = "前端用户默认余额响应")
public class AppUserBalanceResponse {

    /**
     * 账户 ID。
     */
    @Schema(description = "账户 ID", example = "20001")
    private Long accountId;

    /**
     * 货币类型。
     */
    @Schema(description = "货币类型", example = "USDT")
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
    @Schema(description = "账户状态：1 正常，0 禁用", example = "1")
    private Integer status;
}
