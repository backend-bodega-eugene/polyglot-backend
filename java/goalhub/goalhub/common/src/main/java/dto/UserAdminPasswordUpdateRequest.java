package dto;


import lombok.Data;

/**
 * 后台应用用户密码更新请求。
 */
@Data
public class UserAdminPasswordUpdateRequest {

    /**
     * 新密码。
     */
    private String password;
}
