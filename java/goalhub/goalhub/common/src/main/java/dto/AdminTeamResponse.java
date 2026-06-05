package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台球队响应。
 *
 * <p>返回球队基础信息及当前语言下的展示名称。</p>
 */
@Schema(description = "后台球队响应")
@Data
public class AdminTeamResponse {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队 ID", example = "1")
    private Long id;

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

    /**
     * 当前语言名称。
     */
    @Schema(description = "当前语言名称")
    private String name;

    /**
     * 当前语言简称。
     */
    @Schema(description = "当前语言简称")
    private String shortName;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
