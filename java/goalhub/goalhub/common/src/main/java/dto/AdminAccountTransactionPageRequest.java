package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台账户流水分页查询请求。
 */
@Data
@Schema(description = "后台账户流水分页查询请求")
public class AdminAccountTransactionPageRequest {

    /**
     * 用户名筛选条件。
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
     * 最小变动金额。
     */
    @Schema(description = "最小变动金额")
    private BigDecimal minAmount;

    /**
     * 最大变动金额。
     */
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
    @Schema(description = "页码")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量")
    private Integer pageSize;
}
