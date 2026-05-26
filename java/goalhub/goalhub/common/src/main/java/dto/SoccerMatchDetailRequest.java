package dto;

import lombok.Data;

@Data
public class SoccerMatchDetailRequest {

    /**
     * 语言编码，默认 en-US
     */
    private String langCode = "en-US";
}