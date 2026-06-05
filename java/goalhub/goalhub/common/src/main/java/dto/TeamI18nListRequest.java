package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 球队国际化配置列表查询请求。
 *
 * <p>用于按球队 ID 查询该球队下的多语言配置。</p>
 */
@Schema(description = "球队国际化配置列表查询请求")
@Data
public class TeamI18nListRequest {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID", example = "1001")
    private Long teamId;
}
