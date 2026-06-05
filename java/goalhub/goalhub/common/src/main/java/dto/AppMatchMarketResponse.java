package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 前端赛事玩法响应。
 *
 * <p>返回赛事下某个玩法及其可投注选项列表。</p>
 */
@Data
@Schema(description = "前端赛事玩法响应")
public class AppMatchMarketResponse {

    /**
     * 玩法 ID。
     */
    @Schema(description = "玩法 ID", example = "1")
    private Long marketId;

    /**
     * 玩法编码。
     */
    @Schema(description = "玩法编码", example = "MATCH_WINNER")
    private String marketCode;

    /**
     * 玩法名称。
     */
    @Schema(description = "玩法名称", example = "胜平负")
    private String marketName;

    /**
     * 玩法选项列表。
     */
    @Schema(description = "玩法选项列表")
    private List<AppMatchMarketOptionResponse> options;
}
