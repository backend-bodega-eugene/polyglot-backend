package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 后台更新联赛请求。
 *
 * <p>用于后台维护联赛基础信息、主办国家、Logo 和启用状态。</p>
 */
@Schema(description = "后台更新联赛请求")
@Data
public class UpdateLeagueRequest {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID", example = "10")
    @NotNull(message = "parameter.error")
    private Long id;

    /**
     * 联赛编码。
     */
    @Schema(description = "联赛编码", example = "FIFA_WORLD_CUP_2026")
    @NotBlank(message = "parameter.error")
    private String code;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家", example = "United States, Canada, Mexico")
    private String hostCountry;

    /**
     * 联赛 Logo 地址。
     */
    @Schema(description = "联赛 Logo 地址", example = "https://example.com/logo.png")
    private String logoUrl;

    /**
     * 联赛状态。
     */
    @Schema(description = "联赛状态", example = "1")
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer status;
}
