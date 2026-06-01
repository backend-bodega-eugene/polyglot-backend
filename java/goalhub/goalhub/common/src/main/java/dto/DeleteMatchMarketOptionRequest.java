package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除赛事玩法赔率请求。
 */
@Data
@Schema(description = "删除赛事玩法赔率请求")
public class DeleteMatchMarketOptionRequest {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID")
    private Long id;
}
