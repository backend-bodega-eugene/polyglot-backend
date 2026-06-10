package dto;

import lombok.Data;

@Data
public class VerifyFundPasswordRequest {

    private Long userId;

    private String fundPassword;
}