package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新投注玩法选项请求。
 *
 * <p>用于后台维护投注玩法选项的编码、名称、状态和排序。</p>
 */
@Data
@Schema(description = "更新投注玩法选项请求")
public class UpdateBetMarketOptionRequest {

    /**
     * 投注玩法选项 ID。
     */
    @Schema(description = "投注玩法选项 ID", example = "11")
    private Long id;

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID", example = "1")
    private Long marketId;

    /**
     * 玩法选项编码。
     */
    @Schema(description = "玩法选项编码", example = "HOME_WIN")
    private String code;

    /**
     * 玩法选项名称。
     */
    @Schema(description = "玩法选项名称", example = "主胜")
    private String name;

    /**
     * 玩法选项描述。
     */
    @Schema(description = "玩法选项描述", example = "主队获胜")
    private String description;

    /**
     * 玩法选项状态。
     */
    @Schema(description = "玩法选项状态", example = "OPEN")
    private String status;

    /**
     * 排序值。
     */
    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
}
