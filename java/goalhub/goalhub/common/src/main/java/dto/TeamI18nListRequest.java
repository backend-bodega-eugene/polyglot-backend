package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 球队国际化配置列表查询请求。
 */
@Schema(description = "球队国际化配置列表查询请求")
@Data
public class TeamI18nListRequest {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID")
    private Long teamId;
}
