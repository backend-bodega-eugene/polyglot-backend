package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户流水分页查询请求。
 *
 * <p>用于前端用户按币种、业务类型和时间范围分页查询自己的账户流水。</p>
 */
@Data
@Schema(description = "用户流水分页查询请求")
public class AccountTransactionPageRequest {

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
