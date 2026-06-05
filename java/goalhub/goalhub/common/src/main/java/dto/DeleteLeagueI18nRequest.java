package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除联赛国际化配置请求。
 *
 * <p>根据国际化配置 ID 删除指定联赛语言配置。</p>
 */
@Schema(description = "删除联赛国际化配置请求")
@Data
public class DeleteLeagueI18nRequest {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID", example = "1")
    private Long id;
}
