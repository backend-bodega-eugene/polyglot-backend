package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "前端用户默认余额响应")
public class AppUserBalanceResponse {

    @Schema(description = "账户ID")
    private Long accountId;

    @Schema(description = "货币类型")
    private String currencyCode;

    @Schema(description = "账户余额")
    private BigDecimal balance;

    @Schema(description = "冻结余额")
    private BigDecimal frozenBalance;

    @Schema(description = "可用余额")
    private BigDecimal availableBalance;

    @Schema(description = "账户状态")
    private Integer status;
}