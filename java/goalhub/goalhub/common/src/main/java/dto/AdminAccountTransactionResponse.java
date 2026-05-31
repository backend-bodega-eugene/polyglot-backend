package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台账户流水响应。
 */
@Data
@Schema(description = "后台账户流水响应")
public class AdminAccountTransactionResponse {

    /**
     * 流水 ID。
     */
    @Schema(description = "流水ID")
    private Long id;

    /**
     * 账户 ID。
     */
    @Schema(description = "账户ID")
    private Long accountId;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 用户名。
     */
    @Schema(description = "用户名")
    private String username;

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
