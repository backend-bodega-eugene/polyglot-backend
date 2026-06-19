package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "删除联盟球队玩法赔率请求")
public class DeleteLeagueTeamMarketOddsRequest {

    /**
     * 联盟球队玩法赔率配置 ID。
     */
    @Schema(description = "联盟球队玩法赔率配置ID", example = "1")
    private Long id;
}
