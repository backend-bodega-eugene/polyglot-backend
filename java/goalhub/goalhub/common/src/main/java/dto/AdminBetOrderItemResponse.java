package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 后台投注订单明细响应。
 *
 * <p>返回投注订单中单个投注项的比赛、玩法、赔率、金额和判定结果快照。</p>
 */
@Data
@Schema(description = "后台投注订单明细响应")
public class AdminBetOrderItemResponse {

    /**
     * 订单明细 ID。
     */
    @Schema(description = "订单明细 ID", example = "1")
    private Long itemId;

    /**
     * 订单 ID。
     */
    @Schema(description = "订单 ID", example = "10001")
    private Long orderId;

    /**
     * 订单号。
     */
    @Schema(description = "订单号", example = "BO202606040001")
    private String orderNo;

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事ID")
    private Long matchId;

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛ID")
    private Long leagueId;

    /**
     * 联赛名称。
     */
    @Schema(description = "联赛名称")
    private String leagueName;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队ID")
    private Long homeTeamId;

    /**
     * 主队名称。
     */
    @Schema(description = "主队名称")
    private String homeTeamName;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队ID")
    private Long awayTeamId;

    /**
     * 客队名称。
     */
    @Schema(description = "客队名称")
    private String awayTeamName;

    /**
     * 比赛开始时间。
     */
    @Schema(description = "比赛开始时间")
    private LocalDateTime matchStartTime;

    /**
     * 玩法 ID。
     */
    @Schema(description = "玩法ID")
    private Long playId;

    /**
     * 玩法选项 ID。
     */
    @Schema(description = "玩法选项ID")
    private Long optionId;

    /**
     * 玩法编码快照。
     */
    @Schema(description = "玩法编码快照")
    private String playCode;

    /**
     * 玩法名称快照。
     */
    @Schema(description = "玩法名称快照")
    private String playName;

    /**
     * 玩法选项编码快照。
     */
    @Schema(description = "玩法选项编码快照")
    private String optionCode;

    /**
     * 玩法选项名称快照。
     */
    @Schema(description = "玩法选项名称快照")
    private String optionName;

    /**
     * 下注时赔率快照。
     */
    @Schema(description = "下注时赔率快照")
    private BigDecimal odds;

    /**
     * 下注金额。
     */
    @Schema(description = "下注金额")
    private BigDecimal betAmount;

    /**
     * 预计盈利金额。
     */
    @Schema(description = "预计盈利金额")
    private BigDecimal expectedProfit;

    /**
     * 预计返还金额。
     */
    @Schema(description = "预计返还金额")
    private BigDecimal expectedReturn;

    /**
     * 系统判定结果。
     */
    @Schema(description = "系统判定结果")
    private String systemResult;

    /**
     * 赛事结果快照。
     */
    @Schema(description = "赛事结果快照")
    private String matchResultSnapshot;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
