package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "订单扣减默认USDT账户请求")
public class DeductDefaultAccountRequest {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "扣减金额")
    private BigDecimal amount;

    @Schema(description = "业务ID，一般为订单号")
    private String bizId;

    @Schema(description = "备注")
    private String remark;
}