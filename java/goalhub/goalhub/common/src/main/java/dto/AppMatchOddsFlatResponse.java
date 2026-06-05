package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 前端赛事赔率扁平响应。
 *
 * <p>用于返回未按玩法分组的赛事玩法选项赔率列表。</p>
 */
@Data
@Schema(description = "前端赛事赔率扁平响应")
public class AppMatchOddsFlatResponse {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID", example = "1")
    private Long id;

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事 ID", example = "1")
    private Long matchId;

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
     * 玩法选项 ID。
     */
    @Schema(description = "玩法选项 ID", example = "1")
    private Long marketOptionId;

    /**
     * 玩法选项编码。
     */
    @Schema(description = "玩法选项编码", example = "HOME_WIN")
    private String marketOptionCode;

    /**
     * 玩法选项名称。
     */
    @Schema(description = "玩法选项名称", example = "主胜")
    private String marketOptionName;

    /**
     * 赔率。
     */
    @Schema(description = "赔率", example = "1.95")
    private BigDecimal odds;

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
}
