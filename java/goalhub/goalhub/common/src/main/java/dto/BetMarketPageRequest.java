package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 投注玩法分页查询请求。
 *
 * <p>用于后台按关键字和状态分页查询投注玩法。</p>
 */
@Data
@Schema(description = "投注玩法分页查询请求")
public class BetMarketPageRequest {

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

    /**
     * 关键字。
     */
    @Schema(description = "关键字")
    private String keyword;

    /**
     * 投注玩法状态。
     */
    @Schema(description = "投注玩法状态", example = "OPEN")
    private String status;
}
