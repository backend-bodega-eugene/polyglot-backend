package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台联赛响应。
 *
 * <p>返回联赛基础信息及当前语言下的展示名称。</p>
 */
@Schema(description = "后台联赛响应")
@Data
public class AdminLeagueResponse {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "1")
    private Long id;

    /**
     * 联赛编码。
     */
    @Schema(description = "联赛编码", example = "EPL")
    private String code;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家", example = "GB")
    private String hostCountry;

    /**
     * 联赛 Logo 地址。
     */
    @Schema(description = "联赛 Logo 地址")
    private String logoUrl;

    /**
     * 联赛状态。
     */
    @Schema(description = "联赛状态", example = "1")
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
