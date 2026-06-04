package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "前端赛事赛果分页查询请求")
public class AppMatchResultPageRequest {

    @Schema(description = "语言编码")
    private String langCode;

    @Schema(description = "联盟ID")
    private Long leagueId;

    @Schema(description = "球队名称")
    private String teamName;

    @Schema(description = "比赛开始时间")
    private LocalDateTime startTime;

    @Schema(description = "比赛结束时间")
    private LocalDateTime endTime;

    @Schema(description = "页码")
    private Integer pageIndex;

    @Schema(description = "每页数量")
    private Integer pageSize;
}