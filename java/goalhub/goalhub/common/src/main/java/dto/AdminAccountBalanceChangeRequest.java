package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 后台账户余额变更请求。
 */
@Data
@Schema(description = "后台账户余额变更请求")
public class AdminAccountBalanceChangeRequest {

    /**
     * 账户 ID。
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 变更金额。
     */
    @Schema(description = "变更金额")
    private BigDecimal amount;

    /**
     * 备注。
     */
    @Schema(description = "备注")
    private String remark;
}
