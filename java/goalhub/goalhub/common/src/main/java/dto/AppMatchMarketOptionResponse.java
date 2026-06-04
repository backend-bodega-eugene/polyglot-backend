package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "前端赛事玩法选项赔率响应")
public class AppMatchMarketOptionResponse {

    @Schema(description = "赛事玩法赔率ID")
    private Long id;

    @Schema(description = "玩法选项ID")
    private Long marketOptionId;

    @Schema(description = "玩法选项编码")
    private String marketOptionCode;

    @Schema(description = "玩法选项名称")
    private String marketOptionName;

    @Schema(description = "赔率")
    private BigDecimal odds;

    @Schema(description = "投注状态")
    private String betStatus;

    @Schema(description = "排序值")
    private Integer sortOrder;
}