package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台用户账户响应。
 */
@Data
@Schema(description = "后台用户账户响应")
public class AdminUserAccountResponse {

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

    /**
     * 用户创建时间。
     */
    @Schema(description = "用户创建时间")
    private LocalDateTime userCreatedAt;

    /**
     * 账户创建时间。
     */
    @Schema(description = "账户创建时间")
    private LocalDateTime accountCreatedAt;
}
