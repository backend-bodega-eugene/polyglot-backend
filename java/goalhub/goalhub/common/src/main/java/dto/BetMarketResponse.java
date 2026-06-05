package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投注玩法响应。
 *
 * <p>返回投注玩法主配置的基础信息和状态。</p>
 */
@Data
@Schema(description = "投注玩法响应")
public class BetMarketResponse {

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

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间", example = "2026-06-04T12:00:00")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间", example = "2026-06-04T12:30:00")
    private LocalDateTime updatedAt;
}
