package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核赛事结果请求。
 *
 * <p>用于后台审核指定赛事的赛果。</p>
 */
@Data
@Schema(description = "审核赛事结果请求")
public class ApproveMatchResultRequest {

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事 ID", example = "1")
    @NotNull(message = "parameter.error")
    private Long matchId;
}
