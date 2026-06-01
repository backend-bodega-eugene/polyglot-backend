package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台投注订单明细分页查询请求。
 */
@Data
@Schema(description = "后台投注订单明细分页查询请求")
public class AdminBetOrderItemPageRequest {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单ID")
    private Long orderId;

    /**
     * 订单号。
     */
    @Schema(description = "订单号")
    private String orderNo;

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

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码")
    private String langCode;
}
