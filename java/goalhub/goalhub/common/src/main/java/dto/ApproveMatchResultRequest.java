package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 审核赛事结果请求。
 */
@Data
@Schema(description = "审核赛事结果请求")
public class ApproveMatchResultRequest {

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事ID")
    private Long matchId;
}
