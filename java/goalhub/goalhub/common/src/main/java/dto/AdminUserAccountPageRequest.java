package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台用户账户分页查询请求。
 *
 * <p>用于后台按用户名、币种、余额区间、账户状态和用户创建时间筛选账户。</p>
 */
@Data
@Schema(description = "后台用户账户分页查询请求")
public class AdminUserAccountPageRequest {

    /**
     * 用户名筛选条件。
     */
    @Size(max = 50, message = "用户名长度不能超过50")
    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    /**
     * 货币类型。
     */
    @Size(max = 16, message = "货币类型长度不能超过16")
    @Schema(description = "货币类型", example = "USDT")
    private String currencyCode;

    /**
     * 最小余额。
     */
    @DecimalMin(value = "0.00", message = "最小余额不能小于0")
    @Schema(description = "最小余额")
    private BigDecimal minBalance;

    /**
     * 最大余额。
     */
    @DecimalMin(value = "0.00", message = "最大余额不能小于0")
    @Schema(description = "最大余额")
    private BigDecimal maxBalance;

    /**
     * 账户状态。
     */
    @Min(value = 0, message = "账户状态只能是0或1")
    @Max(value = 1, message = "账户状态只能是0或1")
    @Schema(description = "账户状态：1 正常，0 禁用", example = "1")
    private Integer status;

    /**
     * 用户创建开始时间。
     */
    @Schema(description = "用户创建开始时间")
    private LocalDateTime userCreatedStartTime;

    /**
     * 用户创建结束时间。
     */
    @Schema(description = "用户创建结束时间")
    private LocalDateTime userCreatedEndTime;

    /**
     * 页码。
     */
    @Min(value = 1, message = "页码不能小于1")
    @Schema(description = "页码", example = "1")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Min(value = 1, message = "每页数量不能小于1")
    @Max(value = 100, message = "每页数量不能超过100")
    @Schema(description = "每页数量", example = "20")
    private Integer pageSize;
}
