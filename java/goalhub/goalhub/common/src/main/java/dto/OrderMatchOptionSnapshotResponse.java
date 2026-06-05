package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单下单用赛事玩法赔率快照响应。
 *
 * <p>在下单前返回比赛、玩法、选项和赔率快照，确保订单记录使用同一份展示信息。</p>
 */
@Data
@Schema(description = "订单下单用赛事玩法赔率快照响应")
public class OrderMatchOptionSnapshotResponse {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率ID", example = "1")
    private Long matchMarketOptionId;

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事ID", example = "1001")
    private Long matchId;

    /**
     * 赛事编码。
     */
    @Schema(description = "赛事编码", example = "MATCH202606040001")
    private String matchCode;

    /**
     * 赛事名称。
     */
    @Schema(description = "赛事名称", example = "Brazil vs Argentina")
    private String matchName;

    /**
     * 赛事状态。
     */
    @Schema(description = "赛事状态", example = "SCHEDULED")
    private String matchStatus;

    /**
     * 比赛开始时间。
     */
    @Schema(description = "比赛开始时间", example = "2026-06-15T20:00:00")
    private LocalDateTime matchStartTime;

    /**
     * 联盟 ID。
     */
    @Schema(description = "联盟ID", example = "10")
    private Long leagueId;

    /**
     * 联盟名称。
     */
    @Schema(description = "联盟名称", example = "FIFA World Cup 2026")
    private String leagueName;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队ID", example = "1001")
    private Long homeTeamId;

    /**
     * 主队名称。
     */
    @Schema(description = "主队名称", example = "Brazil")
    private String homeTeamName;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队ID", example = "1002")
    private Long awayTeamId;

    /**
     * 客队名称。
     */
    @Schema(description = "客队名称", example = "Argentina")
    private String awayTeamName;

    /**
     * 玩法 ID。
     */
    @Schema(description = "玩法ID", example = "1")
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
    @Schema(description = "玩法选项ID", example = "11")
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
}
