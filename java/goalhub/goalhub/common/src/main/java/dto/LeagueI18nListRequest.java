package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 联赛国际化配置列表查询请求。
 */
@Schema(description = "联赛国际化配置列表查询请求")
@Data
public class LeagueI18nListRequest {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID")
    private Long leagueId;
}
