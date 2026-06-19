package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "冠军赔率快照响应")
public class ChampionOddsSnapshotResponse {

    /**
     * 冠军赔率 ID。
     */
    @Schema(description = "冠军赔率ID", example = "1")
    private Long championOddsId;

    /**
     * 联赛或杯赛 ID。
     */
    @Schema(description = "联赛/杯赛ID", example = "1")
    private Long leagueId;

    /**
     * 联赛或杯赛名称。
     */
    @Schema(description = "联赛/杯赛名称", example = "世界杯")
    private String leagueName;

    /**
     * 球队 ID。
     */
    @Schema(description = "球队ID", example = "1001")
    private Long teamId;

    /**
     * 球队名称。
     */
    @Schema(description = "球队名称", example = "France")
    private String teamName;

    /**
     * 冠军赔率。
     */
    @Schema(description = "冠军赔率", example = "2.50")
    private BigDecimal odds;

    /**
     * 是否在前台展示。
     */
    @Schema(description = "是否可见，1可见，0隐藏", example = "1")
    private Integer visible;

    /**
     * 下注状态。
     */
    @Schema(description = "下注状态", example = "OPEN")
    private String betStatus;
}
