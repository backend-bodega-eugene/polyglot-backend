package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 热门足球比赛查询请求。
 */
@Schema(description = "热门足球比赛查询请求")
@Data
public class SoccerHotMatchRequest {

    /**
     * 语言编码，默认 en-US。
     */
    @Schema(description = "语言编码，默认 en-US", example = "en-US")
    private String langCode = "en-US";

    /**
     * 返回数量，默认 10。
     */
    @Schema(description = "返回数量，默认 10", example = "10")
    private Integer limit = 10;
}
