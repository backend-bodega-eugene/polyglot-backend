package dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppMatchOddsFlatResponse {

    private Long id;

    private Long matchId;

    private Long marketId;

    private String marketCode;

    private String marketName;

    private Long marketOptionId;

    private String marketOptionCode;

    private String marketOptionName;

    private BigDecimal odds;

    private String betStatus;

    private Integer sortOrder;
}