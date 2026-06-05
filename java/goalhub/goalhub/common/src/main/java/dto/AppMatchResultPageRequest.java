package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 前端赛事赛果分页查询请求。
 *
 * <p>用于前端按联赛、球队、语言和比赛时间范围分页查询赛果。</p>
 */
@Data
@Schema(description = "前端赛事赛果分页查询请求")
public class AppMatchResultPageRequest {

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh_CN")
    private String langCode;

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    private Long leagueId;

    /**
     * 球队名称。
     */
    @Schema(description = "球队名称")
    private String teamName;

    /**
     * 比赛开始时间。
     */
    @Schema(description = "比赛开始时间")
    private LocalDateTime startTime;

    /**
     * 比赛结束时间。
     */
    @Schema(description = "比赛结束时间")
    private LocalDateTime endTime;

    /**
     * 页码。
     */
    @Schema(description = "页码", example = "1")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量", example = "20")
    private Integer pageSize;
}
