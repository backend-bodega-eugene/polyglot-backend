package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台用户账户分页查询请求。
 */
@Data
@Schema(description = "后台用户账户分页查询请求")
public class AdminUserAccountPageRequest {

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
     * 最小余额。
     */
    @Schema(description = "最小余额")
    private BigDecimal minBalance;

    /**
     * 最大余额。
     */
    @Schema(description = "最大余额")
    private BigDecimal maxBalance;

    /**
     * 账户状态。
     */
    @Schema(description = "账户状态")
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
    @Schema(description = "页码")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量")
    private Integer pageSize;
}
