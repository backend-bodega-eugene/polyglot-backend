package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "前端下注请求")
public class PlaceBetOrderRequest {

    @Schema(description = "赛事玩法赔率ID")
    private Long matchMarketOptionId;

    @Schema(description = "下注金额")
    private BigDecimal amount;
}