package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户流水分页查询请求。
 */
@Data
@Schema(description = "用户流水分页查询请求")
public class AccountTransactionPageRequest {

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
