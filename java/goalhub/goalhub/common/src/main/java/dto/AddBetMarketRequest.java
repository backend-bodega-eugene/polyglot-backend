package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 新增投注玩法请求。
 *
 * <p>用于后台新增投注玩法主配置。</p>
 */
@Data
@Schema(description = "新增投注玩法请求")
public class AddBetMarketRequest {

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
    @Schema(description = "投注玩法描述")
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
