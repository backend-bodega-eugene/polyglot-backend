package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 足球比赛详情查询请求。
 *
 * <p>用于指定足球比赛详情的返回语言。</p>
 */
@Schema(description = "足球比赛详情查询请求")
@Data
public class SoccerMatchDetailRequest {

    /**
     * 语言编码，默认 en-US。
     */
    @Schema(description = "语言编码，默认 en-US", example = "en-US")
    private String langCode = "en-US";
}
