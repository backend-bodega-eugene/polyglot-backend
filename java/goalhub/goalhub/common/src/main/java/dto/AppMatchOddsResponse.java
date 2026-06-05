package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 前端赛事赔率响应。
 *
 * <p>按赛事聚合返回可投注玩法及其玩法选项赔率。</p>
 */
@Data
@Schema(description = "前端赛事赔率响应")
public class AppMatchOddsResponse {

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事 ID", example = "1")
    private Long matchId;

    /**
     * 玩法列表。
     */
    @Schema(description = "玩法列表")
    private List<AppMatchMarketResponse> markets;
}
