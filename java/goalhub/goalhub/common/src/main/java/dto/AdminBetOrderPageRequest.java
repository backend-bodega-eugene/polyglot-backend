package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台投注订单分页查询请求。
 *
 * <p>用于后台按订单号、用户、币种、状态、判定结果和创建时间筛选投注订单。</p>
 */
@Data
@Schema(description = "后台投注订单分页查询请求")
public class AdminBetOrderPageRequest {

    /**
     * 订单号。
     */
    @Size(max = 64, message = "订单号长度不能超过64")
    @Schema(description = "订单号", example = "BO202606040001")
    private String orderNo;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    /**
     * 币种编码。
     */
    @Size(max = 16, message = "币种编码长度不能超过16")
    @Schema(description = "币种编码", example = "USDT")
    private String currencyCode;

    /**
     * 订单状态。
     */
    @Size(max = 32, message = "订单状态长度不能超过32")
    @Schema(description = "订单状态", example = "PENDING")
    private String status;

    /**
     * 系统判定结果。
     */
    @Size(max = 32, message = "系统判定结果长度不能超过32")
    @Schema(description = "系统判定结果")
    private String systemResult;

    /**
     * 审核结果。
     */
    @Size(max = 32, message = "审核结果长度不能超过32")
    @Schema(description = "审核结果")
    private String reviewResult;

    /**
     * 订单创建开始时间。
     */
    @Schema(description = "订单创建开始时间")
    private LocalDateTime createdStartTime;

    /**
     * 订单创建结束时间。
     */
    @Schema(description = "订单创建结束时间")
    private LocalDateTime createdEndTime;

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
