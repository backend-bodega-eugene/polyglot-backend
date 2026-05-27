package dto;

import lombok.Data;

@Data
public class AdminUserCreateRequest {
    private String username;
    private String password;
    private String nickname;
    private Integer isSuperAdmin;
    private Integer status;
}