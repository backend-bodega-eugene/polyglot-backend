package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台删除比赛请求。
 */
@Schema(description = "后台删除比赛请求")
@Data
public class DeleteMatchRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID")
    private Long id;
}
