package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 赛事玩法赔率响应。
 *
 * <p>返回比赛、投注玩法、玩法选项、赔率和展示状态等完整赔率配置。</p>
 */
@Data
@Schema(description = "赛事玩法赔率响应")
public class MatchMarketOptionResponse {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID", example = "1")
    private Long id;

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1001")
    private Long matchId;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码", example = "MATCH202606040001")
    private String matchCode;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称", example = "曼城 vs 切尔西")
    private String matchName;

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
     * 投注玩法编码。
     */
    @Schema(description = "投注玩法编码", example = "MATCH_WINNER")
    private String marketCode;

    /**
     * 投注玩法名称。
     */
    @Schema(description = "投注玩法名称", example = "胜平负")
    private String marketName;

    /**
     * 投注玩法选项编码。
     */
    @Schema(description = "投注玩法选项编码", example = "HOME_WIN")
    private String marketOptionCode;

    /**
     * 投注玩法选项名称。
     */
    @Schema(description = "投注玩法选项名称", example = "主胜")
    private String marketOptionName;

    /**
     * 赔率。
     */
    @Schema(description = "赔率", example = "1.85")
    private BigDecimal odds;

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
     * 排序值。
     */
    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间", example = "2026-06-04T12:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间", example = "2026-06-04T12:30:00")
    private LocalDateTime updatedAt;
}
