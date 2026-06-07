package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台充值订单详情查询参数。
 */
@Schema(description = "后台充值订单详情查询参数")
@Data
public class AdminDepositOrderDetailRequest {

    /**
     * 充值订单 ID。
     */
    @Schema(description = "充值订单 ID", example = "10001")
    private Long id;
}
