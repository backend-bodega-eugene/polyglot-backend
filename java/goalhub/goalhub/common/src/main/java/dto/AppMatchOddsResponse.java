package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "前端赛事赔率响应")
public class AppMatchOddsResponse {

    @Schema(description = "赛事ID")
    private Long matchId;

    @Schema(description = "玩法列表")
    private List<AppMatchMarketResponse> markets;
}