package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 比赛国际化配置列表查询请求。
 */
@Schema(description = "比赛国际化配置列表查询请求")
@Data
public class MatchI18nListRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID")
    private Long matchId;
}
