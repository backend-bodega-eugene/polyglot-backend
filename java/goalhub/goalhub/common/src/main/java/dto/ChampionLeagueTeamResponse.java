package dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "联赛球队响应")
public class ChampionLeagueTeamResponse {

    /**
     * 球队 ID。
     */
    @Schema(description = "球队ID", example = "1001")
    private Long teamId;

    /**
     * 球队编码。
     */
    @Schema(description = "球队编码", example = "FRA")
    private String teamCode;

    /**
     * 球队名称。
     */
    @Schema(description = "球队名称", example = "France")
    private String teamName;

    /**
     * 球队简称。
     */
    @Schema(description = "球队简称", example = "FRA")
    private String shortName;

    /**
     * 球队 Logo 地址。
     */
    @Schema(description = "球队Logo地址", example = "https://example.com/team/france.png")
    private String logoUrl;
}
