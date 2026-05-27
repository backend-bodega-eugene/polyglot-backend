package dto;

import lombok.Data;

@Data
public class AdminUserUpdateRequest {
    private String nickname;
    private Integer status;
    private Integer isSuperAdmin;
}