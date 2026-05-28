package dto;

import lombok.Data;

/**
 * 后台管理员登录请求。
 */
@Data
public class AdminLoginRequest {

    /**
     * 管理员账号。
     */
    private String username;

    /**
     * 登录密码。
     */
    private String password;
}
