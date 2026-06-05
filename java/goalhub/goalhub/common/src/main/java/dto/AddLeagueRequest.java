package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 后台新增联赛请求。
 *
 * <p>用于后台创建联赛基础信息，国际化名称通过联赛国际化接口维护。</p>
 */
@Schema(description = "后台新增联赛请求")
@Data
public class AddLeagueRequest {

    /**
     * 联赛编码。
     */
    @Schema(description = "联赛编码", example = "EPL")
    @NotBlank(message = "parameter.error")
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
    @Min(value = 0, message = "parameter.error")
    @Max(value = 1, message = "parameter.error")
    private Integer status;
}
