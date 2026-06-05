package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台更新比赛请求。
 *
 * <p>用于后台维护比赛基础信息、参赛球队、开赛时间、主办国家和比赛状态。</p>
 */
@Schema(description = "后台更新比赛请求")
@Data
public class UpdateMatchRequest {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1001")
    @NotNull(message = "parameter.error")
    private Long id;

    /**
     * 所属联赛 ID。
     */
    @Schema(description = "所属联赛 ID", example = "10")
    @NotNull(message = "parameter.error")
    private Long leagueId;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码", example = "MATCH202606040001")
    @NotBlank(message = "parameter.error")
    private String matchCode;

    /**
     * 阶段编码。
     */
    @Schema(description = "阶段编码", example = "FINAL")
    private String stageCode;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队 ID", example = "1001")
    @NotNull(message = "parameter.error")
    private Long homeTeamId;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队 ID", example = "1002")
    @NotNull(message = "parameter.error")
    private Long awayTeamId;

    /**
     * 计划开赛时间，UTC 时间。
     */
    @Schema(description = "计划开赛时间，UTC 时间", example = "2026-06-15T20:00:00")
    @NotNull(message = "parameter.error")
    private LocalDateTime scheduledStartTimeUtc;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家", example = "United States")
    private String hostCountry;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态", example = "SCHEDULED")
    private String status;
}
