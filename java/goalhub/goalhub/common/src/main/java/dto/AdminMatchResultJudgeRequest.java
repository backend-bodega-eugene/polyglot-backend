package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 后台赛事订单系统预判请求。
 */
@Data
@Schema(description = "后台赛事订单系统预判请求")
public class AdminMatchResultJudgeRequest {

    /**
     * 赛事 ID。
     */
    @Schema(description = "赛事ID", example = "10001")
    private Long matchId;

    /**
     * 操作备注。
     */
    @Schema(description = "操作备注")
    private String remark;

    /**
     * 管理员 ID。
     */
    @Schema(description = "管理员ID")
    private Long adminId;

    /**
     * 管理员用户名。
     */
    @Schema(description = "管理员用户名")
    private String adminUsername;

    /**
     * 赛果信息。
     */
    @Schema(description = "赛果信息")
    private SaveMatchResultRequest matchResult;
}
