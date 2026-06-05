package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新投注玩法请求。
 *
 * <p>用于后台维护投注玩法的编码、名称、状态和排序。</p>
 */
@Data
@Schema(description = "更新投注玩法请求")
public class UpdateBetMarketRequest {

    /**
     * 投注玩法 ID。
     */
    @Schema(description = "投注玩法 ID", example = "1")
    private Long id;

    /**
     * 投注玩法编码。
     */
    @Schema(description = "投注玩法编码", example = "MATCH_WINNER")
    private String code;

    /**
     * 投注玩法名称。
     */
    @Schema(description = "投注玩法名称", example = "胜平负")
    private String name;

    /**
     * 投注玩法描述。
     */
    @Schema(description = "投注玩法描述", example = "预测比赛常规时间胜负平结果")
    private String description;

    /**
     * 投注玩法状态。
     */
    @Schema(description = "投注玩法状态", example = "OPEN")
    private String status;

    /**
     * 排序值。
     */
    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
}
