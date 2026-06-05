package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除赛事玩法赔率请求。
 *
 * <p>根据赛事玩法赔率 ID 删除指定赔率配置。</p>
 */
@Data
@Schema(description = "删除赛事玩法赔率请求")
public class DeleteMatchMarketOptionRequest {

    /**
     * 赛事玩法赔率 ID。
     */
    @Schema(description = "赛事玩法赔率 ID", example = "1")
    private Long id;
}
