package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 赛事玩法赔率分页查询请求。
 *
 * <p>支持按比赛、玩法、玩法选项、可见状态和投注状态筛选赔率配置。</p>
 */
@Data
@Schema(description = "赛事玩法赔率分页查询请求")
public class MatchMarketOptionPageRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1001")
    private Long matchId;

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID", example = "1")
    private Long marketId;

    /**
     * 投注玩法选项 ID。
     */
    @Schema(description = "投注玩法选项 ID", example = "11")
    private Long marketOptionId;

    /**
     * 关键字。
     */
    @Schema(description = "关键字", example = "主胜")
    private String keyword;

    /**
     * 是否前端可见。
     */
    @Schema(description = "是否前端可见", example = "1")
    private Integer visible;

    /**
     * 投注状态。
     */
    @Schema(description = "投注状态", example = "OPEN")
    private String betStatus;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;
}
