package dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AppBetOrderItemResponse {

    private Long itemId;

    private Long orderId;

    private String orderNo;

    private Long matchId;

    private Long leagueId;

    private String leagueName;

    private Long homeTeamId;

    private String homeTeamName;

    private Long awayTeamId;

    private String awayTeamName;

    private LocalDateTime matchStartTime;

    private Long playId;

    private Long optionId;

    private String playCode;

    private String playName;

    private String optionCode;

    private String optionName;

    private BigDecimal odds;

    private BigDecimal betAmount;

    private BigDecimal expectedProfit;

    private BigDecimal expectedReturn;

    private String systemResult;

    private String matchResultSnapshot;

    private LocalDateTime createdAt;
}