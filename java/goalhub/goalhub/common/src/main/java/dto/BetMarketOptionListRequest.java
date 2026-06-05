package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 投注玩法选项列表查询请求。
 *
 * <p>用于按投注玩法 ID 和可选状态查询玩法选项列表。</p>
 */
@Data
@Schema(description = "投注玩法选项列表查询请求")
public class BetMarketOptionListRequest {

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID", example = "1")
    private Long marketId;

    /**
     * 玩法选项状态。
     */
    @Schema(description = "玩法选项状态", example = "OPEN")
    private String status;
}
