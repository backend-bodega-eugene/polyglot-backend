package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 删除球队国际化配置请求。
 */
@Schema(description = "删除球队国际化配置请求")
@Data
public class DeleteTeamI18nRequest {

    /**
     * 国际化配置 ID。
     */
    @Schema(description = "国际化配置 ID")
    private Long id;
}
