package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 投注玩法选项列表查询请求。
 */
@Data
@Schema(description = "投注玩法选项列表查询请求")
public class BetMarketOptionListRequest {

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID")
    private Long marketId;

    /**
     * 玩法选项状态。
     */
    @Schema(description = "玩法选项状态")
    private String status;
}
