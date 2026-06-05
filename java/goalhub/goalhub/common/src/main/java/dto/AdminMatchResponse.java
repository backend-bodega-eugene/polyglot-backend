package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台比赛响应。
 *
 * <p>返回后台赛事列表和详情所需的比赛基础信息及当前语言展示字段。</p>
 */
@Schema(description = "后台比赛响应")
@Data
public class AdminMatchResponse {

    /**
     * 比赛 ID。
     */
    @Schema(description = "比赛 ID", example = "1")
    private Long id;

    /**
     * 所属联赛 ID。
     */
    @Schema(description = "所属联赛 ID", example = "1")
    private Long leagueId;

    /**
     * 联赛名称。
     */
    @Schema(description = "联赛名称")
    private String leagueName;

    /**
     * 比赛编码。
     */
    @Schema(description = "比赛编码", example = "MATCH_20260604_001")
    private String matchCode;

    /**
     * 比赛名称。
     */
    @Schema(description = "比赛名称")
    private String matchName;

    /**
     * 阶段编码。
     */
    @Schema(description = "阶段编码")
    private String stageCode;

    /**
     * 阶段名称。
     */
    @Schema(description = "阶段名称")
    private String stageName;

    /**
     * 主队 ID。
     */
    @Schema(description = "主队 ID", example = "10")
    private Long homeTeamId;

    /**
     * 主队名称。
     */
    @Schema(description = "主队名称")
    private String homeTeamName;

    /**
     * 客队 ID。
     */
    @Schema(description = "客队 ID", example = "11")
    private Long awayTeamId;

    /**
     * 客队名称。
     */
    @Schema(description = "客队名称")
    private String awayTeamName;

    /**
     * 计划开赛时间，UTC 时间。
     */
    @Schema(description = "计划开赛时间，UTC 时间")
    private LocalDateTime scheduledStartTimeUtc;

    /**
     * 实际开赛时间，UTC 时间。
     */
    @Schema(description = "实际开赛时间，UTC 时间")
    private LocalDateTime actualStartTimeUtc;

    /**
     * 实际结束时间，UTC 时间。
     */
    @Schema(description = "实际结束时间，UTC 时间")
    private LocalDateTime actualEndTimeUtc;

    /**
     * 主办国家。
     */
    @Schema(description = "主办国家")
    private String hostCountry;

    /**
     * 比赛城市。
     */
    @Schema(description = "比赛城市")
    private String city;

    /**
     * 比赛场馆。
     */
    @Schema(description = "比赛场馆")
    private String venue;

    /**
     * 比赛状态。
     */
    @Schema(description = "比赛状态", example = "SCHEDULED")
    private String status;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
