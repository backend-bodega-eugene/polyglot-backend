package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除投注玩法选项请求。
 */
@Data
@Schema(description = "删除投注玩法选项请求")
public class DeleteBetMarketOptionRequest {

    /**
     * 投注玩法选项 ID。
     */
    @Schema(description = "投注玩法选项 ID")
    private Long id;
}
