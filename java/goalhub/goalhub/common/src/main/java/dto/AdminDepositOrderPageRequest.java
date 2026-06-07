package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台充值订单分页查询参数。
 */
@Schema(description = "后台充值订单分页查询参数")
@Data
public class AdminDepositOrderPageRequest {

    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID", example = "10001")
    private Long userId;

    /**
     * 充值订单号。
     */
    @Schema(description = "充值订单号")
    private String orderNo;

    /**
     * 币种编码。
     */
    @Schema(description = "币种编码", example = "USDT")
    private String currencyCode;

    /**
     * 订单状态。
     */
    @Schema(description = "订单状态：PENDING / APPROVED / REJECTED", example = "PENDING")
    private String status;

    /**
     * 链类型。
     */
    @Schema(description = "链类型", example = "TRC20")
    private String chainType;

    /**
     * 链上交易哈希。
     */
    @Schema(description = "链上交易哈希")
    private String txHash;

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
