package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 投注玩法分页查询请求。
 */
@Data
@Schema(description = "投注玩法分页查询请求")
public class BetMarketPageRequest {

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
     * 关键字。
     */
    @Schema(description = "关键字")
    private String keyword;

    /**
     * 投注玩法状态。
     */
    @Schema(description = "投注玩法状态")
    private String status;
}
