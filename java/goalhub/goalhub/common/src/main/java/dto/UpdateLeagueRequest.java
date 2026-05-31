package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台更新联赛请求。
 */
@Schema(description = "后台更新联赛请求")
@Data
public class UpdateLeagueRequest {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID")
    private Long id;

    /**
     * 联赛编码。
     */
    @Schema(description = "联赛编码")
    private String code;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家")
    private String hostCountry;

    /**
     * 联赛 Logo 地址。
     */
    @Schema(description = "联赛 Logo 地址")
    private String logoUrl;

    /**
     * 联赛状态。
     */
    @Schema(description = "联赛状态")
    private Integer status;
}
