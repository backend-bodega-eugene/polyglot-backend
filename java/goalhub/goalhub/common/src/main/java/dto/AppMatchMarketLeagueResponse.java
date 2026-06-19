package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * App 联赛维度赛事玩法赔率响应。
 *
 * <p>按联赛聚合比赛列表，用于前端展示联赛下的赛事玩法赔率。</p>
 */
@Data
@Schema(description = "App 联赛维度赛事玩法赔率响应")
public class AppMatchMarketLeagueResponse {

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
     * 联赛或杯赛 Logo 地址。
     */
    @Schema(description = "联赛/杯赛Logo地址")
    private String leagueLogoUrl;

    /**
     * 联赛下的比赛列表。
     */
    @Schema(description = "联赛下的比赛列表")
    private List<AppMatchMarketMatchResponse> matches;
}
