package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投注玩法响应。
 */
@Data
@Schema(description = "投注玩法响应")
public class BetMarketResponse {

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID")
    private Long id;

    /**
     * 投注玩法编码。
     */
    @Schema(description = "投注玩法编码")
    private String code;

    /**
     * 投注玩法名称。
     */
    @Schema(description = "投注玩法名称")
    private String name;

    /**
     * 投注玩法描述。
     */
    @Schema(description = "投注玩法描述")
    private String description;

    /**
     * 投注玩法状态。
     */
    @Schema(description = "投注玩法状态")
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
