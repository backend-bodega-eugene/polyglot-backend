package dto;

import lombok.Data;

@Data
public class UserProfileUpdateRequest {

    private String email;
    private String phone;
    private String nickname;
    private String avatarUrl;

}