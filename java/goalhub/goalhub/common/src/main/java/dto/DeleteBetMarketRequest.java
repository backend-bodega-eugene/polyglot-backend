package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除投注玩法请求。
 *
 * <p>根据投注玩法 ID 删除指定玩法配置。</p>
 */
@Data
@Schema(description = "删除投注玩法请求")
public class DeleteBetMarketRequest {

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID", example = "1")
    private Long id;
}
