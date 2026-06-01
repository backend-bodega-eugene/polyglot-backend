package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新投注玩法选项请求。
 */
@Data
@Schema(description = "更新投注玩法选项请求")
public class UpdateBetMarketOptionRequest {

    /**
     * 投注玩法选项 ID。
     */
    @Schema(description = "投注玩法选项 ID")
    private Long id;

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID")
    private Long marketId;

    /**
     * 玩法选项编码。
     */
    @Schema(description = "玩法选项编码")
    private String code;

    /**
     * 玩法选项名称。
     */
    @Schema(description = "玩法选项名称")
    private String name;

    /**
     * 玩法选项描述。
     */
    @Schema(description = "玩法选项描述")
    private String description;

    /**
     * 玩法选项状态。
     */
    @Schema(description = "玩法选项状态")
    private String status;

    /**
     * 排序值。
     */
    @Schema(description = "排序值")
    private Integer sortOrder;
}
