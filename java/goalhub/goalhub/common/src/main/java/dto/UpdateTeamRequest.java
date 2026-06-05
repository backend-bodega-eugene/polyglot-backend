package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台更新球队请求。
 *
 * <p>用于后台维护球队编码、Logo 和启用状态。</p>
 */
@Schema(description = "后台更新球队请求")
@Data
public class UpdateTeamRequest {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID", example = "1001")
    private Long id;

    /**
     * 球队编码。
     */
    @Schema(description = "球队编码", example = "BRA")
    private String code;

    /**
     * 球队 Logo 地址。
     */
    @Schema(description = "球队 Logo 地址", example = "https://example.com/team-logo.png")
    private String logoUrl;

    /**
     * 球队状态。
     */
    @Schema(description = "球队状态", example = "1")
    private Integer status;
}
