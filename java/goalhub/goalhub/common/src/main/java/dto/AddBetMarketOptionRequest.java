package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新增投注玩法选项请求。
 *
 * <p>用于后台在指定投注玩法下新增一个可配置的玩法选项。</p>
 */
@Data
@Schema(description = "新增投注玩法选项请求")
public class AddBetMarketOptionRequest {

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
    @Schema(description = "玩法选项描述")
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
