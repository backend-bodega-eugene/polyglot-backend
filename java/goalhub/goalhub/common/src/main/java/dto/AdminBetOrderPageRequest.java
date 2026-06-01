package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台投注订单分页查询请求。
 */
@Data
@Schema(description = "后台投注订单分页查询请求")
public class AdminBetOrderPageRequest {

    /**
     * 订单号。
     */
    @Schema(description = "订单号")
    private String orderNo;

    /**
     * 用户 ID。
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 币种编码。
     */
    @Schema(description = "币种编码")
    private String currencyCode;

    /**
     * 订单状态。
     */
    @Schema(description = "订单状态")
    private String status;

    /**
     * 系统判定结果。
     */
    @Schema(description = "系统判定结果")
    private String systemResult;

    /**
     * 审核结果。
     */
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
    @Schema(description = "页码")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量")
    private Integer pageSize;
}
