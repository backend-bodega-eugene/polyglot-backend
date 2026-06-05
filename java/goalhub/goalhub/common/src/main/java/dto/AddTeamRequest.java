package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台新增球队请求。
 *
 * <p>用于后台创建球队基础信息，国际化名称通过球队国际化接口维护。</p>
 */
@Schema(description = "后台新增球队请求")
@Data
public class AddTeamRequest {

    /**
     * 球队编码。
     */
    @Schema(description = "球队编码", example = "TEAM_001")
    private String code;

    /**
     * 球队 Logo 地址。
     */
    @Schema(description = "球队 Logo 地址")
    private String logoUrl;

    /**
     * 球队状态。
     */
    @Schema(description = "球队状态", example = "1")
    private Integer status;
}
