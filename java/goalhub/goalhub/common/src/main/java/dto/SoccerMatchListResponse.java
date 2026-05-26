package dto;

import lombok.Data;

@Data
public class SoccerMatchListResponse {

    private Long id;

    private String matchName;

    private String homeTeamName;

    private String awayTeamName;

    private String scheduledStartTimeUtc;

    private String status;
}