package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台删除联赛请求。
 */
@Schema(description = "后台删除联赛请求")
@Data
public class DeleteLeagueRequest {

    /**
     * 联赛 ID。
     */
    @Schema(description = "联赛 ID")
    private Long id;
}
