package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台更新球队请求。
 */
@Schema(description = "后台更新球队请求")
@Data
public class UpdateTeamRequest {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID")
    private Long id;

    /**
     * 球队编码。
     */
    @Schema(description = "球队编码")
    private String code;

    /**
     * 球队 Logo 地址。
     */
    @Schema(description = "球队 Logo 地址")
    private String logoUrl;

    /**
     * 球队状态。
     */
    @Schema(description = "球队状态")
    private Integer status;
}
