package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 比赛国际化配置列表查询请求。
 *
 * <p>用于按比赛 ID 查询该比赛下的多语言配置。</p>
 */
@Schema(description = "比赛国际化配置列表查询请求")
@Data
public class MatchI18nListRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1")
    private Long matchId;
}
