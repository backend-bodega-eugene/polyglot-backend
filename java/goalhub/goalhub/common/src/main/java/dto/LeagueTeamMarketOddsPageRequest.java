package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "联盟球队玩法赔率分页查询请求")
public class LeagueTeamMarketOddsPageRequest {

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
     * 联赛或杯赛 ID。
     */
    @Schema(description = "联赛/杯赛ID", example = "1")
    private Long leagueId;

    /**
     * 玩法 ID。
     */
    @Schema(description = "玩法ID", example = "1001")
    private Long playId;

    /**
     * 玩法编码。
     */
    @Schema(description = "玩法编码", example = "CHAMPION")
    private String playCode;

    /**
     * 球队 ID。
     */
    @Schema(description = "球队ID", example = "1001")
    private Long teamId;

    /**
     * 查询关键字。
     */
    @Schema(description = "关键字，联赛/玩法/球队", example = "法国")
    private String keyword;

    /**
     * 是否在前台展示。
     */
    @Schema(description = "是否可见", example = "1")
    private Integer visible;

    /**
     * 下注状态。
     */
    @Schema(description = "下注状态", example = "OPEN")
    private String betStatus;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;
}
