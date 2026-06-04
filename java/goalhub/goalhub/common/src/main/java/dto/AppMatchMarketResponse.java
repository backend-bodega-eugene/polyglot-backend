package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "前端赛事玩法响应")
public class AppMatchMarketResponse {

    @Schema(description = "玩法ID")
    private Long marketId;

    @Schema(description = "玩法编码")
    private String marketCode;

    @Schema(description = "玩法名称")
    private String marketName;

    @Schema(description = "玩法选项列表")
    private List<AppMatchMarketOptionResponse> options;
}