package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除投注玩法请求。
 */
@Data
@Schema(description = "删除投注玩法请求")
public class DeleteBetMarketRequest {

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID")
    private Long id;
}
