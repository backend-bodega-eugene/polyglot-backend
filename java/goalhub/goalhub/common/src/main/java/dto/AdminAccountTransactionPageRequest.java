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
 * 后台账户流水分页查询请求。
 *
 * <p>用于后台按用户、币种、业务类型、金额区间和时间范围筛选账户流水。</p>
 */
@Data
@Schema(description = "后台账户流水分页查询请求")
public class AdminAccountTransactionPageRequest {

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
     * 业务类型。
     */
    @Size(max = 32, message = "业务类型长度不能超过32")
    @Schema(description = "业务类型", example = "BET")
    private String bizType;

    /**
     * 最小变动金额。
     */
    @DecimalMin(value = "0.00", message = "最小变动金额不能小于0")
    @Schema(description = "最小变动金额")
    private BigDecimal minAmount;

    /**
     * 最大变动金额。
     */
    @DecimalMin(value = "0.00", message = "最大变动金额不能小于0")
    @Schema(description = "最大变动金额")
    private BigDecimal maxAmount;

    /**
     * 查询开始时间。
     */
    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    /**
     * 查询结束时间。
     */
    @Schema(description = "结束时间")
    private LocalDateTime endTime;

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
