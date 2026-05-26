package dto;

import lombok.Data;

@Data
public class SoccerHotMatchRequest {

    private String langCode = "en-US";

    private Integer limit = 10;
}