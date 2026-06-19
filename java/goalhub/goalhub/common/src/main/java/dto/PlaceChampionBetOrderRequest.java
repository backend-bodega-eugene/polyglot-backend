package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "冠军下注请求")
public class PlaceChampionBetOrderRequest {

    /**
     * 冠军赔率 ID。
     */
    @Schema(description = "冠军赔率ID", example = "1")
    private Long championOddsId;

    /**
     * 下注金额。
     */
    @Schema(description = "下注金额", example = "100")
    private BigDecimal amount;
}
