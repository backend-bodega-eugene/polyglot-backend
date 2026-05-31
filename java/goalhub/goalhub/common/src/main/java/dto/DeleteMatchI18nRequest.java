package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除比赛国际化配置请求。
 */
@Schema(description = "删除比赛国际化配置请求")
@Data
public class DeleteMatchI18nRequest {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID")
    private Long id;
}
