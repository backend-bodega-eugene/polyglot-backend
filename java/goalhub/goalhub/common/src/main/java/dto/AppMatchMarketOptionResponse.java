package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 前端赛事玩法选项赔率响应。
 *
 * <p>返回赛事下某个玩法选项的赔率和投注状态。</p>
 */
@Data
@Schema(description = "前端赛事玩法选项赔率响应")
public class AppMatchMarketOptionResponse {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID", example = "1")
    private Long id;

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
