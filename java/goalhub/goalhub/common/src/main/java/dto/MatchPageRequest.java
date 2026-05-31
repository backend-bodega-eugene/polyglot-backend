package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台比赛分页查询请求。
 */
@Schema(description = "后台比赛分页查询请求")
@Data
public class MatchPageRequest {

    /**
     * 页码。
     */
    @Schema(description = "页码")
    private Integer pageIndex;

    /**
     * 每页数量。
     */
    @Schema(description = "每页数量")
    private Integer pageSize;

    /**
     * 语言编码。
     */
    @Schema(description = "语言编码")
    private String langCode;

    /**
     * 赛事名称关键字。
     */
    @Schema(description = "赛事名称关键字")
    private String keyword;
}
