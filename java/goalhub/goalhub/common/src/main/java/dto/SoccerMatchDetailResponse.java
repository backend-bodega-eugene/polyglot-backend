package dto;

import lombok.Data;

@Data
public class SoccerMatchDetailResponse {

    private Long id;

    private Long leagueId;

    private String leagueName;

    private String matchCode;

    private String matchName;

    private String stageCode;

    private String stageName;

    private Long homeTeamId;

    private String homeTeamName;

    private String homeTeamShortName;

    private Long awayTeamId;

    private String awayTeamName;

    private String awayTeamShortName;

    private String scheduledStartTimeUtc;

    private String actualStartTimeUtc;

    private String actualEndTimeUtc;

    private String hostCountry;

    private String city;

    private String venue;

    private String status;
}