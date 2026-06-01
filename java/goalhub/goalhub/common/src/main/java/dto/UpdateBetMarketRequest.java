package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新投注玩法请求。
 */
@Data
@Schema(description = "更新投注玩法请求")
public class UpdateBetMarketRequest {

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
}
