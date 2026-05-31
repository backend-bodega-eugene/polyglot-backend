package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除联赛国际化配置请求。
 */
@Schema(description = "删除联赛国际化配置请求")
@Data
public class DeleteLeagueI18nRequest {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID")
    private Long id;
}
