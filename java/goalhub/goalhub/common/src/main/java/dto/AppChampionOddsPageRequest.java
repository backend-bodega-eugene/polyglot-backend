package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "前端冠军赔率分页查询请求")
public class AppChampionOddsPageRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageIndex;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize;

    @Schema(description = "联赛/杯赛ID", example = "1")
    private Long leagueId;

    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;
}