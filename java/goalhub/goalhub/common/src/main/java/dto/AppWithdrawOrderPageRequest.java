package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * App 提现订单分页查询请求。
 *
 * <p>用于 App 端按订单号、币种、状态和时间范围分页查询当前用户的提现订单。</p>
 */
@Schema(description = "App 提现订单分页查询请求")
@Data
public class AppWithdrawOrderPageRequest {

    /**
     * 提现订单号。
     */
    @Schema(description = "提现订单号")
    private String orderNo;

    /**
     * 币种编码。
     */
    @Schema(description = "币种编码", example = "USDT")
    private String currencyCode;

    /**
     * 订单状态。
     */
    @Schema(description = "订单状态", example = "PENDING")
    private String status;

    /**
     * 查询开始时间。
     */
    @Schema(description = "查询开始时间")
    private LocalDateTime startTime;

    /**
     * 查询结束时间。
     */
    @Schema(description = "查询结束时间")
    private LocalDateTime endTime;

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "20")
    private Integer pageSize;
}
