package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 足球比赛分页查询请求。
 */
@Schema(description = "足球比赛分页查询请求")
@Data
public class SoccerMatchPageRequest {

    /**
     * 页码，从 1 开始。
     */
    @Schema(description = "页码，从 1 开始", example = "1")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "10")
    private Integer pageSize;

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    private Long leagueId;

    /**
     * 语言编码，默认 en-US。
     */
    @Schema(description = "语言编码，默认 en-US", example = "en-US")
    private String langCode = "en-US";

    /**
     * 查询开始时间，UTC 时间字符串。
     */
    @Schema(description = "查询开始时间，UTC 时间字符串", example = "2026-06-01T00:00:00Z")
    private String startTimeUtc;

    /**
     * 查询结束时间，UTC 时间字符串。
     */
    @Schema(description = "查询结束时间，UTC 时间字符串", example = "2026-06-30T23:59:59Z")
    private String endTimeUtc;

    /**
     * 球队关键字。
     */
    @Schema(description = "球队关键字", example = "Brazil")
    private String teamKeyword;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态", example = "SCHEDULED")
    private String status;

    /**
     * 通用关键字。
     */
    @Schema(description = "通用关键字", example = "final")
    private String keyword;
}
