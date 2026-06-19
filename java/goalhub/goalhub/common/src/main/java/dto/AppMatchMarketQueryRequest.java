package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "前端赛事玩法赔率聚合查询请求")
public class AppMatchMarketQueryRequest {

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
     * 查询关键字。
     */
    @Schema(description = "关键字，支持联赛、比赛或球队名称", example = "France")
    private String keyword;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "en-US")
    private String langCode;
}
