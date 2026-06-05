package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除投注玩法选项请求。
 *
 * <p>根据投注玩法选项 ID 删除指定选项配置。</p>
 */
@Data
@Schema(description = "删除投注玩法选项请求")
public class DeleteBetMarketOptionRequest {

    /**
     * 投注玩法选项 ID。
     */
    @Schema(description = "投注玩法选项 ID", example = "1")
    private Long id;
}
