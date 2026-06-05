package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 联赛国际化配置列表查询请求。
 *
 * <p>用于按联赛 ID 查询该联赛下的多语言配置。</p>
 */
@Schema(description = "联赛国际化配置列表查询请求")
@Data
public class LeagueI18nListRequest {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    private Long leagueId;
}
