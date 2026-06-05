package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台删除比赛请求。
 *
 * <p>根据比赛 ID 删除指定比赛记录。</p>
 */
@Schema(description = "后台删除比赛请求")
@Data
public class DeleteMatchRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1")
    private Long id;
}
