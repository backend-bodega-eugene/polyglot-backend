package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "更新冠军赔率请求")
public class UpdateChampionMarketOddsRequest {

    /**
     * 冠军赔率配置 ID。
     */
    @Schema(description = "冠军赔率配置ID", example = "1")
    private Long id;

    /**
     * 冠军赔率。
     */
    @Schema(description = "冠军赔率", example = "2.50")
    private BigDecimal odds;

    /**
     * 是否在前台展示。
     */
    @Schema(description = "是否可见，1可见，0隐藏", example = "1")
    private Integer visible;

    /**
     * 下注状态。
     */
    @Schema(description = "下注状态", example = "OPEN")
    private String betStatus;

    /**
     * 排序值。
     */
    @Schema(description = "排序值，数值越小越靠前", example = "10")
    private Integer sortOrder;

    /**
     * 球队名称快照。
     */
    @Schema(description = "球队名称快照", example = "France")
    private String teamNameSnapshot;
}
