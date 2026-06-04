package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "订单下单用赛事玩法赔率快照响应")
public class OrderMatchOptionSnapshotResponse {

    @Schema(description = "赛事玩法赔率ID")
    private Long matchMarketOptionId;

    @Schema(description = "赛事ID")
    private Long matchId;

    @Schema(description = "赛事编码")
    private String matchCode;

    @Schema(description = "赛事名称")
    private String matchName;

    @Schema(description = "赛事状态")
    private String matchStatus;

    @Schema(description = "比赛开始时间")
    private LocalDateTime matchStartTime;

    @Schema(description = "联盟ID")
    private Long leagueId;

    @Schema(description = "联盟名称")
    private String leagueName;

    @Schema(description = "主队ID")
    private Long homeTeamId;

    @Schema(description = "主队名称")
    private String homeTeamName;

    @Schema(description = "客队ID")
    private Long awayTeamId;

    @Schema(description = "客队名称")
    private String awayTeamName;

    @Schema(description = "玩法ID")
    private Long marketId;

    @Schema(description = "玩法编码")
    private String marketCode;

    @Schema(description = "玩法名称")
    private String marketName;

    @Schema(description = "玩法选项ID")
    private Long marketOptionId;

    @Schema(description = "玩法选项编码")
    private String marketOptionCode;

    @Schema(description = "玩法选项名称")
    private String marketOptionName;

    @Schema(description = "赔率")
    private BigDecimal odds;

    @Schema(description = "是否前端可见")
    private Integer visible;

    @Schema(description = "投注状态")
    private String betStatus;
}