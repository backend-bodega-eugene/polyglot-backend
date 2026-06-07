package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台赛事结果分页查询请求。
 *
 * <p>用于后台按赛事、球队、语言、状态和时间范围分页查询赛果。</p>
 */
@Data
@Schema(description = "后台赛事结果分页查询请求")
public class AdminMatchResultPageRequest {

    /**
     * 赛事名称筛选条件。
     */
    @Schema(description = "赛事名称")
    private String matchName;

    /**
     * 球队名称筛选条件。
     */
    @Schema(description = "球队名称")
    private String teamName;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;

    /**
     * 赛事状态筛选条件。
     */
    @Schema(description = "赛事状态", example = "FINISHED")
    private String matchStatus;

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
