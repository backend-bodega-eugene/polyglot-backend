package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台新增比赛请求。
 *
 * <p>用于后台创建足球比赛基础信息。</p>
 */
@Schema(description = "后台新增比赛请求")
@Data
public class AddMatchRequest {

    /**
     * 所属联赛 ID。
     */
    @Schema(description = "所属联赛 ID", example = "1")
    @NotNull(message = "parameter.error")
    private Long leagueId;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码", example = "MATCH_20260604_001")
    @NotBlank(message = "parameter.error")
    private String matchCode;

    /**
     * 阶段编码。
     */
    @Schema(description = "阶段编码")
    private String stageCode;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队 ID", example = "10")
    @NotNull(message = "parameter.error")
    private Long homeTeamId;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队 ID", example = "11")
    @NotNull(message = "parameter.error")
    private Long awayTeamId;

    /**
     * 计划开赛时间，UTC 时间。
     */
    @Schema(description = "计划开赛时间，UTC 时间")
    @NotNull(message = "parameter.error")
    private LocalDateTime scheduledStartTimeUtc;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家", example = "CN")
    private String hostCountry;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态", example = "SCHEDULED")
    private String status;
}
