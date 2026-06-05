package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单扣减默认 USDT 账户响应。
 *
 * <p>返回扣减账户和扣减前后的余额信息。</p>
 */
@Data
@Schema(description = "订单扣减默认USDT账户响应")
public class DeductDefaultAccountResponse {

    /**
     * 账户 ID。
     */
    @Schema(description = "账户ID", example = "20001")
    private Long accountId;

    /**
     * 币种编码。
     */
    @Schema(description = "币种编码", example = "USDT")
    private String currencyCode;

    /**
     * 扣减前余额。
     */
    @Schema(description = "扣减前余额", example = "100.00")
    private BigDecimal balanceBefore;

    /**
     * 扣减后余额。
     */
    @Schema(description = "扣减后余额", example = "50.00")
    private BigDecimal balanceAfter;
}
