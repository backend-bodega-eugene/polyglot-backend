package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台联赛分页查询请求。
 *
 * <p>支持按语言和联赛名称关键字分页查询联赛列表。</p>
 */
@Schema(description = "后台联赛分页查询请求")
@Data
public class LeaguePageRequest {

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
     * 语言编码。
     */
    @Schema(description = "语言编码", example = "zh-CN")
    private String langCode;

    /**
     * 联赛名称关键字。
     */
    @Schema(description = "联赛名称关键字", example = "英超")
    private String keyword;
}
