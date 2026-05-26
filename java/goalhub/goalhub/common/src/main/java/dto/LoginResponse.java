package dto;

import lombok.Data;

@Data
public class LoginResponse {

    private Long userId;

    private String username;

    private String token;
}