package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 后台投注订单明细分页查询请求。
 *
 * <p>用于后台按订单 ID 或订单号分页查询投注订单明细。</p>
 */
@Data
@Schema(description = "后台投注订单明细分页查询请求")
public class AdminBetOrderItemPageRequest {

    /**
     * 订单 ID。
     */
    @Schema(description = "订单 ID", example = "10001")
    private Long orderId;

    /**
     * 订单号。
     */
    @Size(max = 64, message = "订单号长度不能超过64")
    @Schema(description = "订单号", example = "BO202606040001")
    private String orderNo;

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

    /**
     * 语言编码。
     */
    @Size(max = 16, message = "语言编码长度不能超过16")
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;
}
