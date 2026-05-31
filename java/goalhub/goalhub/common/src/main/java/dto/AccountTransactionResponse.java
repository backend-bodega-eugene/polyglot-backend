package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户流水响应。
 */
@Data
@Schema(description = "账户流水响应")
public class AccountTransactionResponse {

    /**
     * 流水 ID。
     */
    @Schema(description = "流水ID")
    private Long id;

    /**
     * 货币类型。
     */
    @Schema(description = "货币类型")
    private String currencyCode;

    /**
     * 业务类型。
     */
    @Schema(description = "业务类型")
    private String bizType;

    /**
     * 业务 ID。
     */
    @Schema(description = "业务ID")
    private String bizId;

    /**
     * 变动金额。
     */
    @Schema(description = "变动金额")
    private BigDecimal changeAmount;

    /**
     * 变动前余额。
     */
    @Schema(description = "变动前余额")
    private BigDecimal beforeBalance;

    /**
     * 变动后余额。
     */
    @Schema(description = "变动后余额")
    private BigDecimal afterBalance;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
