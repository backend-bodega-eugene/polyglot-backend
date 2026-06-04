package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "订单扣减默认USDT账户响应")
public class DeductDefaultAccountResponse {

    @Schema(description = "账户ID")
    private Long accountId;

    @Schema(description = "币种编码")
    private String currencyCode;

    @Schema(description = "扣减前余额")
    private BigDecimal balanceBefore;

    @Schema(description = "扣减后余额")
    private BigDecimal balanceAfter;
}