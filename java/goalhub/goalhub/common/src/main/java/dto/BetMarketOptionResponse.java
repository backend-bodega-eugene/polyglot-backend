package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投注玩法选项响应。
 */
@Data
@Schema(description = "投注玩法选项响应")
public class BetMarketOptionResponse {

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

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
