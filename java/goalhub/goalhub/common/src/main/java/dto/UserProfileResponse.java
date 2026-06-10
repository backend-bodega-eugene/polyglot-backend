package dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileResponse {

    private Long id;
    private String username;
    private String email;
    private String phone;
    private String nickname;
    private String avatarUrl;
    private Integer status;
    private Boolean hasFundPassword;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}